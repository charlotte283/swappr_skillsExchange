package com.skillexchange.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.skillexchange.app.data.LocalStore
import com.skillexchange.app.data.Provider
import com.skillexchange.app.data.RequestStatus
import com.skillexchange.app.data.RequestType
import com.skillexchange.app.data.SeedData
import com.skillexchange.app.data.Service
import com.skillexchange.app.data.ServiceRequest
import com.skillexchange.app.data.UserProfile
import com.skillexchange.app.ml.EstimatorLoader
import com.skillexchange.app.ml.ExchangeChecker
import com.skillexchange.app.ml.ExchangeResult
import com.skillexchange.app.ml.ValueEstimator
import com.skillexchange.app.reco.Match
import com.skillexchange.app.reco.Recommender
import java.util.UUID

class AppViewModel(app: Application) : AndroidViewModel(app) {
    /** The Ridge model from the notebook (or the fallback if ridge_model.json is not in assets). */
    val estimator: ValueEstimator = EstimatorLoader.load(app)
    private val store = LocalStore(app)

    val providers = mutableStateListOf<Provider>().apply { addAll(SeedData.providers) }
    val providerServices = mutableStateListOf<Service>().apply { addAll(SeedData.services(estimator)) }
    val myServices = mutableStateListOf<Service>().apply { addAll(store.loadServices()) }
    val requests = mutableStateListOf<ServiceRequest>().apply { addAll(store.loadRequests()) }
    val favouriteIds = mutableStateListOf<String>()
    var profile by mutableStateOf(store.loadProfile())
        private set
    var isLoggedIn by mutableStateOf(store.isLoggedIn())
        private set

    fun login(email: String, pass: String): Boolean {
        val success = store.loginUser(email, pass)
        if (success) {
            profile = store.loadProfile()
            isLoggedIn = true
        }
        return success
    }

    fun register(email: String, pass: String, name: String, area: String, bio: String): Boolean {
        val p = UserProfile(name = name, area = area, bio = bio)
        val success = store.registerUser(email, pass, p)
        if (success) {
            profile = p
            isLoggedIn = true
        }
        return success
    }

    fun logout() {
        store.logout()
        isLoggedIn = false
    }

    fun toggleFavourite(serviceId: String) {
        if (favouriteIds.contains(serviceId)) {
            favouriteIds.remove(serviceId)
        } else {
            favouriteIds.add(serviceId)
        }
    }

    fun isFavourite(serviceId: String): Boolean = favouriteIds.contains(serviceId)

    fun matches(): List<Match> = Recommender.recommend(profile, myServices, providers, providerServices)

    fun serviceById(id: String): Service? = providerServices.firstOrNull { it.id == id } ?: myServices.firstOrNull { it.id == id }
    fun providerById(id: String): Provider? = providers.firstOrNull { it.id == id } ?: if (id == "me") Provider("me", profile.name, profile.area, profile.bio, 5.0, 1, profile.wants, profile.profilePhoto) else null

    /** Live estimate computed dynamically from user inputs (title keywords, category, experience level & effort hours). */
    fun previewValue(
        title: String,
        category: String,
        subcat: String,
        experienceLevel: String = "Intermediate",
        estimatedHours: Int = 5
    ): Double {
        val baseEur = estimator.estimate(title.trim().lowercase(), category, subcat, null, 0)
        val levelMult = when (experienceLevel.lowercase()) {
            "beginner" -> 0.80
            "intermediate" -> 1.00
            "advanced" -> 1.30
            "expert" -> 1.65
            else -> 1.00
        }
        val hoursMult = (0.5 + (estimatedHours.coerceIn(1, 100) * 0.10)).coerceIn(0.5, 3.5)
        return baseEur * levelMult * hoursMult
    }

    fun explain(title: String, category: String, subcat: String) =
        estimator.explain(title.trim().lowercase(), category, subcat, null, 0)

    fun addService(
        title: String,
        category: String,
        subcat: String,
        experienceLevel: String = "Intermediate",
        estimatedHours: Int = 5
    ) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val computedValue = previewValue(clean, category, subcat, experienceLevel, estimatedHours)
        myServices += Service(UUID.randomUUID().toString(), "me", clean, category, subcat, computedValue)
        store.saveServices(myServices)
    }

    fun removeService(id: String) {
        myServices.removeAll { it.id == id }
        store.saveServices(myServices)
    }

    fun updateProfile(p: UserProfile) {
        profile = p
        store.saveProfile(p)
    }

    fun check(mine: Service, theirs: Service): ExchangeResult {
        val provider = providerById(theirs.ownerId)
        val providerWants = provider?.wants ?: emptySet()
        return ExchangeChecker.check(mine, theirs, myServices, estimator.reference, providerWants)
    }

    fun sendExchange(theirs: Service, mine: Service, result: ExchangeResult) {
        val provider = providerById(theirs.ownerId) ?: return
        val newReq = ServiceRequest(
            id = UUID.randomUUID().toString(), providerId = provider.id, providerName = provider.name,
            theirTitle = theirs.title, theirValueEur = theirs.valueEur, type = RequestType.EXCHANGE,
            myTitle = mine.title, myValueEur = mine.valueEur,
            topUpEur = if (result.fair) 0.0 else result.gap,
        )
        requests.add(0, newReq)
        store.saveRequests(requests)
        NotificationHelper.notifyNewRequest(getApplication(), theirs.title, provider.name)
    }

    fun sendPayment(theirs: Service) {
        val provider = providerById(theirs.ownerId) ?: return
        val newReq = ServiceRequest(
            id = UUID.randomUUID().toString(), providerId = provider.id, providerName = provider.name,
            theirTitle = theirs.title, theirValueEur = theirs.valueEur, type = RequestType.PAY,
        )
        requests.add(0, newReq)
        store.saveRequests(requests)
        NotificationHelper.notifyNewRequest(getApplication(), theirs.title, provider.name)
    }

    /** Prototype only: moves a request along. Triggers notification on status changes. */
    fun advance(id: String) {
        val i = requests.indexOfFirst { it.id == id }
        if (i < 0) return
        val r = requests[i]
        val next = when (r.status) {
            RequestStatus.PENDING -> RequestStatus.ACCEPTED
            RequestStatus.ACCEPTED -> RequestStatus.COMPLETED
            RequestStatus.COMPLETED -> return
        }
        requests[i] = r.copy(status = next)
        store.saveRequests(requests)
        NotificationHelper.notifyStatusChange(getApplication(), r.theirTitle, next.name, r.providerName)
    }

    /** Saves the star rating and folds it into the provider's average. */
    fun rate(id: String, stars: Int) {
        val i = requests.indexOfFirst { it.id == id }
        if (i < 0 || requests[i].myRating != null) return
        val r = requests[i]
        requests[i] = r.copy(myRating = stars)
        store.saveRequests(requests)
        val pi = providers.indexOfFirst { it.id == r.providerId }
        if (pi >= 0) {
            val p = providers[pi]
            val n = p.reviews + 1
            providers[pi] = p.copy(rating = (p.rating * p.reviews + stars) / n, reviews = n)
        }
    }
}

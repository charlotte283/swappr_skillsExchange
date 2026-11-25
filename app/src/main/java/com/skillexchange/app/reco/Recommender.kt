package com.skillexchange.app.reco

import com.skillexchange.app.data.AREAS
import com.skillexchange.app.data.Provider
import com.skillexchange.app.data.Service
import com.skillexchange.app.data.UserProfile
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Match(
    val service: Service,
    val provider: Provider,
    val score: Double,
    val distanceKm: Double,
    val reasons: List<String>,
)

/**
 * Suggests providers / swap partners from skills, location and ratings (the concept note's
 * recommendation feature). It is a transparent weighted score, not a learned model.
 */
object Recommender {
    private const val W_NEED = 0.40       // does the service match what I am looking for?
    private const val W_SWAP = 0.25       // does the provider want a skill I offer? (good swap partner)
    private const val W_NEAR = 0.20       // how close are they?
    private const val W_QUALITY = 0.15    // rating, adjusted for how many reviews back it up

    fun recommend(
        me: UserProfile,
        myServices: List<Service>,
        providers: List<Provider>,
        services: List<Service>,
    ): List<Match> {
        val myArea = AREAS.firstOrNull { it.name == me.area } ?: AREAS.first()
        val myCategories = myServices.map { it.category }.toSet()

        return services.mapNotNull { s ->
            val p = providers.firstOrNull { it.id == s.ownerId } ?: return@mapNotNull null
            val area = AREAS.firstOrNull { it.name == p.area } ?: return@mapNotNull null
            val km = haversineKm(myArea.lat, myArea.lon, area.lat, area.lon)

            val need = if (s.category in me.wants) 1.0 else 0.1
            val swap = if (p.wants.any { it in myCategories }) 1.0 else 0.0
            val near = exp(-km / 10.0)
            // 4.5 is a typical rating; with few reviews the score is pulled towards it
            val smoothed = (p.rating * p.reviews + 4.5 * 5) / (p.reviews + 5)
            val quality = min(max((smoothed - 3.0) / 2.0, 0.0), 1.0)

            val reasons = ArrayList<String>()
            if (need == 1.0) reasons += "Matches what you are looking for"
            if (swap == 1.0) reasons += "Wants a skill you offer"
            if (km < 5) reasons += "Nearby"
            if (p.rating >= 4.8 && p.reviews >= 20) reasons += "Highly rated"

            Match(s, p, W_NEED * need + W_SWAP * swap + W_NEAR * near + W_QUALITY * quality, km, reasons)
        }.sortedByDescending { it.score }
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180
        val dLon = (lon2 - lon1) * PI / 180
        val a = sin(dLat / 2).pow(2) + cos(lat1 * PI / 180) * cos(lat2 * PI / 180) * sin(dLon / 2).pow(2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }
}

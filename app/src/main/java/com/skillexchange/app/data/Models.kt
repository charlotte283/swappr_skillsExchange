package com.skillexchange.app.data

import java.util.Locale

object Config {
    /**
     * The model predicts EUR (Fiverr data). This is only used to show an approximate KSh figure.
     * Replace with a live rate / Kenyan calibration before launch (see notebook "Limitations").
     */
    const val KES_PER_EUR = 150.0
}

fun formatValue(eur: Double): String =
    String.format(Locale.US, "€%.2f  ≈ KSh %,.0f", eur, eur * Config.KES_PER_EUR)

fun formatKes(eur: Double): String =
    String.format(Locale.US, "KSh %,.0f", eur * Config.KES_PER_EUR)

data class Area(val name: String, val lat: Double, val lon: Double)

val AREAS = listOf(
    Area("Kilimani", -1.2921, 36.7872),
    Area("Westlands", -1.2676, 36.8108),
    Area("Kasarani", -1.2216, 36.8984),
    Area("Embakasi", -1.3190, 36.8940),
    Area("Langata", -1.3500, 36.7500),
    Area("Ruaka", -1.2050, 36.7760),
    Area("Rongai", -1.3960, 36.7640),
    Area("Kahawa", -1.1850, 36.9250),
)

data class UploadedFile(
    val name: String,
    val uri: String,
    val size: String = "",
    val mimeType: String = ""
)

data class UserProfile(
    val name: String = "You",
    val area: String = "Kilimani",
    val bio: String = "Passionate learner & skill exchanger.",
    /** categories the user is looking for - drives recommendations */
    val wants: Set<String> = setOf("Programming & Tech"),
    val profilePhoto: String = "",
    val photos: List<String> = emptyList(),
    val uploadedFiles: List<UploadedFile> = emptyList()
)

data class Provider(
    val id: String,
    val name: String,
    val area: String,
    val bio: String,
    val rating: Double,
    val reviews: Int,
    /** categories this person would like in return for their own service */
    val wants: Set<String>,
    val avatarUrl: String = "",
)

data class Service(
    val id: String,
    val ownerId: String,
    val title: String,
    val category: String,
    val subcat: String,
    val valueEur: Double,
    val rating: Double? = null,
    val reviews: Int = 0,
    val imageUrl: String = "",
)

enum class RequestType { EXCHANGE, PAY }
enum class RequestStatus { PENDING, ACCEPTED, COMPLETED }

data class ServiceRequest(
    val id: String,
    val providerId: String,
    val providerName: String,
    val theirTitle: String,
    val theirValueEur: Double,
    val type: RequestType,
    val myTitle: String? = null,
    val myValueEur: Double = 0.0,
    /** extra cash one side adds to balance the swap (0 if none) */
    val topUpEur: Double = 0.0,
    val status: RequestStatus = RequestStatus.PENDING,
    val myRating: Int? = null,
    val pitchMessage: String = "",
)

package com.skillexchange.app.ml

import com.skillexchange.app.data.Service
import kotlin.math.abs
import kotlin.math.max

data class Suggestion(
    val label: String,
    val valueEur: Double,
    val fromMyServices: Boolean,
    val matchesWant: Boolean = false
)

data class ExchangeResult(
    val valueMine: Double,
    val valueTheirs: Double,
    val gap: Double,
    val gapShare: Double,
    val fair: Boolean,
    /** true when the service I offer is worth less than the one I am asking for */
    val iOfferLess: Boolean,
    val suggestions: List<Suggestion>,
)

/** The "fair-exchange check" from notebook section 5, enhanced to align with advertiser requested skills. */
object ExchangeChecker {
    /** A gap of up to 15% of the bigger value counts as fair (same default as the notebook). */
    const val TOLERANCE = 0.15

    fun check(
        mine: Service,
        theirs: Service,
        otherMine: List<Service>,
        reference: List<ReferenceListing>,
        providerWants: Set<String> = emptySet(),
        tolerance: Double = TOLERANCE,
    ): ExchangeResult {
        val a = mine.valueEur
        val b = theirs.valueEur
        val gap = abs(a - b)
        val share = if (max(a, b) > 0) gap / max(a, b) else 0.0
        val fair = share <= tolerance
        val iOfferLess = a < b

        val suggestions = ArrayList<Suggestion>()
        if (!fair && iOfferLess) {
            // Option 2a: another service of mine, prioritizing skills that match what the advertiser is asking for
            otherMine.filter { it.id != mine.id }
                .sortedWith(
                    compareByDescending<Service> { s ->
                        providerWants.any { w -> s.category.contains(w, ignoreCase = true) || s.subcat.contains(w, ignoreCase = true) }
                    }.thenBy { abs(it.valueEur - gap) }
                )
                .take(2)
                .forEach { s ->
                    val matchesWant = providerWants.any { w -> s.category.contains(w, ignoreCase = true) || s.subcat.contains(w, ignoreCase = true) }
                    suggestions += Suggestion(s.title, s.valueEur, fromMyServices = true, matchesWant = matchesWant)
                }

            // Option 2b: examples of real listings (by sub-category) priced close to the gap
            reference.sortedBy { abs(it.price - gap) }
                .distinctBy { it.subcat }
                .take(2)
                .forEach { suggestions += Suggestion("A small ${it.subcat} job", it.price, fromMyServices = false) }
        }
        return ExchangeResult(a, b, gap, share, fair, iOfferLess, suggestions)
    }
}

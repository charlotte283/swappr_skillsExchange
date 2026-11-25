package com.skillexchange.app.ml

import android.content.Context
import org.json.JSONObject
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.expm1
import kotlin.math.ln1p
import kotlin.math.max
import kotlin.math.sqrt

/** A real listing from the training data, used to suggest "extra services" of a given value. */
data class ReferenceListing(val service: String, val subcat: String, val category: String, val price: Double)

/** One reason behind an estimate, e.g. "Sub-category: Legal Writing" with an effect of +23%. */
data class Factor(val label: String, val effectPercent: Double)

/**
 * Anything that can put a value (in EUR, like the notebook's Fiverr data) on a service.
 * [RidgeModel] is the real model from ridge_base.ipynb; [FallbackEstimator] is used until
 * ridge_model.json has been added to app/src/main/assets.
 */
interface ValueEstimator {
    val isRidge: Boolean
    val description: String
    val taxonomy: Map<String, List<String>>
    val reference: List<ReferenceListing>

    fun estimate(service: String, category: String, subcat: String, rating: Double?, reviews: Int): Double

    /** The biggest things that pushed the estimate up or down (empty for the fallback). */
    fun explain(service: String, category: String, subcat: String, rating: Double?, reviews: Int): List<Factor> =
        emptyList()
}

internal class NumParam(val impute: Double, val mean: Double, val scale: Double, val coef: Double)
internal class CatParam(val index: Map<String, Int>, val coef: DoubleArray)
internal class Term(val idf: Double, val weight: Double)

/**
 * Ridge Regression exactly as trained in the notebook:
 *   log1p(price) = intercept + numeric(median-fill + StandardScaler) + one-hot(Category, Subcat)
 *                  + TF-IDF(title, 1-2 word n-grams, English stop words, L2 norm)
 *   price (EUR)  = expm1(log1p(price))
 */
class RidgeModel private constructor(
    private val intercept: Double,
    private val numeric: Map<String, NumParam>,
    private val categorical: Map<String, CatParam>,
    private val stopWords: Set<String>,
    private val terms: Map<String, Term>,
    override val taxonomy: Map<String, List<String>>,
    override val reference: List<ReferenceListing>,
    private val metricsText: String,
) : ValueEstimator {

    override val isRidge = true
    override val description = "Ridge Regression model ($metricsText)"

    override fun estimate(service: String, category: String, subcat: String, rating: Double?, reviews: Int): Double {
        val logPrice = intercept + contributions(service, category, subcat, rating, reviews).sumOf { it.second }
        return max(expm1(logPrice), MIN_VALUE_EUR)
    }

    override fun explain(service: String, category: String, subcat: String, rating: Double?, reviews: Int): List<Factor> =
        contributions(service, category, subcat, rating, reviews)
            .filter { abs(it.second) >= 0.05 }
            .sortedByDescending { abs(it.second) }
            .take(3)
            .map { Factor(it.first, expm1(it.second) * 100.0) }

    /** Every (label, contribution in log-price) pair. Their sum plus the intercept is the prediction. */
    private fun contributions(
        service: String, category: String, subcat: String, rating: Double?, reviews: Int,
    ): List<Pair<String, Double>> {
        val out = ArrayList<Pair<String, Double>>()

        // numeric features: median-fill, then (x - mean) / scale, then multiply by coefficient
        val raw = mapOf(
            "rating" to (rating ?: numeric.getValue("rating").impute),
            "log_reviews" to ln1p(reviews.toDouble()),
            "has_reviews" to (if (reviews > 0) 1.0 else 0.0),
        )
        val names = mapOf("rating" to "Rating", "log_reviews" to "Number of reviews", "has_reviews" to "Has reviews")
        for ((key, p) in numeric) {
            val x = raw.getValue(key)
            out += names.getValue(key) to p.coef * (x - p.mean) / p.scale
        }

        // one-hot features (unknown categories are ignored, like handle_unknown="ignore")
        categorical["Category"]?.let { c -> c.index[category]?.let { out += "Category: $category" to c.coef[it] } }
        categorical["Subcat"]?.let { c -> c.index[subcat]?.let { out += "Sub-category: $subcat" to c.coef[it] } }

        // TF-IDF of the title
        val tokens = tokenize(service).filter { it !in stopWords }
        val grams = ArrayList<String>(tokens)
        for (i in 0 until tokens.size - 1) grams += tokens[i] + " " + tokens[i + 1]
        val counts = HashMap<String, Int>()
        for (g in grams) if (g in terms) counts[g] = (counts[g] ?: 0) + 1
        val weights = counts.mapValues { (g, c) -> c * terms.getValue(g).idf }
        val norm = sqrt(weights.values.sumOf { it * it })
        if (norm > 0.0) {
            for ((g, w) in weights) out += "Keyword \"$g\"" to (w / norm) * terms.getValue(g).weight
        }
        return out
    }

    companion object {
        const val MIN_VALUE_EUR = 1.0
        // Same as scikit-learn's default token_pattern r"(?u)\b\w\w+\b"
        private val TOKEN: Pattern = Pattern.compile("(?u)\\b\\w\\w+\\b")

        private fun tokenize(text: String): List<String> {
            val m = TOKEN.matcher(text.lowercase(Locale.ROOT))
            val list = ArrayList<String>()
            while (m.find()) list += m.group()
            return list
        }

        fun fromJson(text: String): RidgeModel {
            val o = JSONObject(text)

            val numObj = o.getJSONObject("numeric")
            val numeric = LinkedHashMap<String, NumParam>()
            for (key in listOf("rating", "log_reviews", "has_reviews")) {
                val n = numObj.getJSONObject(key)
                numeric[key] = NumParam(n.getDouble("impute"), n.getDouble("mean"), n.getDouble("scale"), n.getDouble("coef"))
            }

            val catObj = o.getJSONObject("categorical")
            val categorical = HashMap<String, CatParam>()
            for (key in catObj.keys()) {
                val c = catObj.getJSONObject(key)
                val values = c.getJSONArray("values")
                val coef = c.getJSONArray("coef")
                val index = HashMap<String, Int>()
                for (i in 0 until values.length()) index[values.getString(i)] = i
                categorical[key] = CatParam(index, DoubleArray(coef.length()) { coef.getDouble(it) })
            }

            val tf = o.getJSONObject("tfidf")
            val stop = tf.getJSONArray("stop_words")
            val stopWords = HashSet<String>()
            for (i in 0 until stop.length()) stopWords += stop.getString(i)
            val termArr = tf.getJSONArray("terms")
            val terms = HashMap<String, Term>(termArr.length() * 2)
            for (i in 0 until termArr.length()) {
                val t = termArr.getJSONObject(i)
                terms[t.getString("t")] = Term(t.getDouble("idf"), t.getDouble("w"))
            }

            val taxObj = o.getJSONObject("taxonomy")
            val taxonomy = sortedMapOf<String, List<String>>()
            for (cat in taxObj.keys()) {
                val arr = taxObj.getJSONArray(cat)
                taxonomy[cat] = List(arr.length()) { arr.getString(it) }
            }

            val refArr = o.optJSONArray("reference_listings")
            val reference = if (refArr == null) emptyList() else List(refArr.length()) {
                val r = refArr.getJSONObject(it)
                ReferenceListing(r.getString("service"), r.getString("subcat"), r.getString("category"), r.getDouble("price"))
            }

            val m = o.optJSONObject("metrics")
            val metricsText = if (m == null) "from ridge_model.json" else
                String.format(Locale.US, "alpha %.0f, test R² %.2f, median error €%.2f",
                    m.optDouble("alpha"), m.optDouble("test_r2"), m.optDouble("test_median_error_eur"))

            return RidgeModel(o.getDouble("intercept"), numeric, categorical, stopWords, terms, taxonomy, reference, metricsText)
        }
    }
}

/**
 * Used when assets/ridge_model.json is missing. It returns the median price of the category, using
 * the per-category medians printed in notebook section 3 (Observation 3). Much rougher than Ridge.
 */
class FallbackEstimator : ValueEstimator {
    override val isRidge = false
    override val description = "Built-in category medians (add ridge_model.json for the Ridge model)"

    private val medians = mapOf(
        "Video & Animation" to 35.55, "Programming & Tech" to 31.11, "Business" to 26.66,
        "Writing & Translation" to 26.66, "Digital Marketing" to 26.66, "Music & Audio" to 22.22,
        "Lifestyle" to 8.89,
    )

    // Sub-categories that appear in the notebook's outputs, grouped by their category.
    override val taxonomy = sortedMapOf(
        "Business" to listOf("Business Plans"),
        "Digital Marketing" to listOf("Social Media Marketing"),
        "Lifestyle" to listOf("Cooking Lessons", "Greeting Cards & Videos", "Local Photography"),
        "Music & Audio" to listOf("Podcast Editing", "Songwriters"),
        "Programming & Tech" to listOf("Data Analysis & Reports", "Website Builders & CMS"),
        "Video & Animation" to listOf("Whiteboard & Animated Explainers"),
        "Writing & Translation" to listOf("Book & eBook Writing", "Legal Writing", "Transcripts"),
    )
    override val reference: List<ReferenceListing> = emptyList()

    override fun estimate(service: String, category: String, subcat: String, rating: Double?, reviews: Int): Double =
        medians[category] ?: 22.22
}

object EstimatorLoader {
    const val MODEL_FILE = "ridge_model.json"

    fun load(context: Context): ValueEstimator =
        try {
            context.assets.open(MODEL_FILE).bufferedReader().use { RidgeModel.fromJson(it.readText()) }
        } catch (e: Exception) {
            FallbackEstimator()
        }
}

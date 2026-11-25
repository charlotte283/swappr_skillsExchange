package com.skillexchange.app.ml

import java.util.Locale
import kotlin.math.min

/**
 * Fuzzy & Semantic Search Engine:
 * Accommodates typos (Levenshtein edit distance), synonyms, alternate wording & tokenized prefix matching.
 */
object FuzzySearch {

    private val SYNONYMS = mapOf(
        "coding" to listOf("programming", "developer", "software", "web", "tech", "code"),
        "developer" to listOf("programming", "tech", "software", "web", "code"),
        "code" to listOf("programming", "developer", "tech", "software"),
        "web" to listOf("website", "wix", "wordpress", "html", "css", "programming"),
        "website" to listOf("wix", "web", "programming", "site", "page"),
        "logo" to listOf("design", "branding", "graphics", "artwork"),
        "graphics" to listOf("design", "logo", "branding", "artwork"),
        "photo" to listOf("photography", "picture", "camera", "shoot"),
        "photography" to listOf("photo", "picture", "camera", "shoot"),
        "math" to listOf("tutoring", "teach", "mathematics", "lesson"),
        "cook" to listOf("cooking", "swahili", "food", "chef", "dishes", "recipe"),
        "food" to listOf("cooking", "chef", "dishes", "swahili"),
        "write" to listOf("writing", "translation", "ebook", "content", "copy"),
        "legal" to listOf("privacy", "gdpr", "terms", "policy", "lawyer"),
        "privacy" to listOf("legal", "gdpr", "terms", "policy"),
        "audio" to listOf("podcast", "sound", "music", "master", "editing"),
        "podcast" to listOf("audio", "sound", "music", "master", "editing"),
    )

    fun matches(queryRaw: String, textRaw: String): Boolean {
        if (queryRaw.isBlank()) return true
        val query = queryRaw.trim().lowercase(Locale.ROOT)
        val text = textRaw.trim().lowercase(Locale.ROOT)

        // 1. Direct Substring Match
        if (text.contains(query)) return true

        val queryTokens = query.split(Regex("\\s+")).filter { it.length > 1 }
        val textTokens = text.split(Regex("[\\s.,/\\-_]+")).filter { it.length > 1 }

        if (queryTokens.isEmpty() || textTokens.isEmpty()) return false

        // 2. Evaluate each token in query against text tokens using Levenshtein distance & Synonyms
        return queryTokens.all { qToken ->
            val directMatch = textTokens.any { tToken ->
                tToken.contains(qToken) || qToken.contains(tToken) ||
                        levenshteinDistance(qToken, tToken) <= maxEditDistance(qToken)
            }
            if (directMatch) return@all true

            // Synonym expansion
            val synonyms = SYNONYMS[qToken].orEmpty() + SYNONYMS.entries.filter { it.value.contains(qToken) }.map { it.key }
            synonyms.any { syn ->
                textTokens.any { tToken ->
                    tToken.contains(syn) || syn.contains(tToken) ||
                            levenshteinDistance(syn, tToken) <= maxEditDistance(syn)
                }
            }
        }
    }

    private fun maxEditDistance(token: String): Int = when {
        token.length <= 3 -> 0
        token.length <= 6 -> 1
        else -> 2
    }

    fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val dp = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var prev = i - 1
            dp[0] = i
            for (j in 1..s2.length) {
                val temp = dp[j]
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return dp[s2.length]
    }
}

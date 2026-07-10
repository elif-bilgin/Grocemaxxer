package com.grocemaxxer.shared

/**
 * Guesses which [StoreSection] a freeform grocery item name belongs to,
 * using whole-word/phrase matching against [sectionKeywords] so that e.g.
 * "eggplant" is not mistaken for "egg" (Dairy) just because it contains
 * that substring.
 */
object ItemCategorizer {

    private val tokenRegex = Regex("[a-z0-9]+")

    fun categorize(rawName: String): StoreSection {
        val normalized = rawName.trim().lowercase()
        if (normalized.isEmpty()) return StoreSection.OTHER

        val rawTokens = tokenRegex.findAll(normalized).map { it.value }.toList()
        if (rawTokens.isEmpty()) return StoreSection.OTHER

        val singularTokens = rawTokens.map { singularize(it) }
        val tokenForms = (rawTokens + singularTokens).toSet()

        var bestSection: StoreSection? = null
        var bestKeywordLength = -1

        for (section in StoreSection.entries) {
            val keywords = sectionKeywords[section] ?: continue
            for (keyword in keywords) {
                val matches = if (keyword.contains(' ')) {
                    containsSubsequence(singularTokens, keyword.split(' '))
                } else {
                    tokenForms.contains(keyword)
                }
                if (matches && keyword.length > bestKeywordLength) {
                    bestKeywordLength = keyword.length
                    bestSection = section
                }
            }
        }

        return bestSection ?: StoreSection.OTHER
    }

    /** True if [sub] appears as a contiguous run within [tokens], e.g. matching plural phrases. */
    private fun containsSubsequence(tokens: List<String>, sub: List<String>): Boolean {
        if (sub.isEmpty() || sub.size > tokens.size) return false
        for (start in 0..tokens.size - sub.size) {
            if ((0 until sub.size).all { tokens[start + it] == sub[it] }) return true
        }
        return false
    }

    /** Very small English singularizer, good enough for common grocery nouns. */
    private fun singularize(token: String): String = when {
        token.endsWith("ies") && token.length > 3 -> token.dropLast(3) + "y"
        token.endsWith("oes") && token.length > 3 -> token.dropLast(2)
        token.endsWith("ches") || token.endsWith("shes") || token.endsWith("xes") ->
            token.dropLast(2)
        token.endsWith("s") && !token.endsWith("ss") && token.length > 3 -> token.dropLast(1)
        else -> token
    }
}

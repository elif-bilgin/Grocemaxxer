package com.grocemaxxer.shared

/** "olive OIL" -> "Olive Oil" -- clean, consistent list entries. */
fun String.toTitleCase(): String =
    trim()
        .split(Regex("\\s+"))
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.titlecase() }
        }

/** Key used for duplicate detection: case- and whitespace-insensitive. */
fun String.toDedupeKey(): String = trim().lowercase().replace(Regex("\\s+"), " ")

/**
 * Filters [candidates] down to names not already present in
 * [existingNames] (nor duplicated within the batch itself), comparing by
 * [toDedupeKey]. Order of first appearance is preserved.
 */
fun filterNewNames(existingNames: List<String>, candidates: List<String>): List<String> {
    val seen = existingNames.map { it.toDedupeKey() }.toMutableSet()
    return candidates.filter { candidate ->
        val key = candidate.toDedupeKey()
        key.isNotEmpty() && seen.add(key)
    }
}

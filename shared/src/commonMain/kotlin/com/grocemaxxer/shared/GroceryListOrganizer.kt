package com.grocemaxxer.shared

/**
 * Turns a flat list of items into a checklist grouped by store section and
 * ordered by [StoreSection.order]. Walking the resulting list top to bottom
 * visits each department at most once, in a fixed low-backtracking path
 * through the store, which is the practical way to minimize time spent
 * shopping without needing a live map of the specific store.
 */
object GroceryListOrganizer {

    fun organize(items: List<GroceryItem>): List<SectionGroup> {
        return items
            .groupBy { it.section }
            .entries
            .sortedBy { it.key.order }
            .map { (section, sectionItems) ->
                SectionGroup(section, sectionItems.sortedBy { it.name.lowercase() })
            }
    }
}

/** Splits pasted/typed freeform text into individual item names. */
object GroceryInputParser {

    fun parse(rawInput: String): List<String> {
        return rawInput
            .split(',', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}

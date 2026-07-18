package com.grocemaxxer.shared

/**
 * Turns a flat list of items into a checklist grouped by store section and
 * ordered by the selected [Store]'s speed-run ranking. Walking the
 * resulting list top to bottom visits each department at most once, in a
 * path tuned to that store's layout. Within each section, unchecked items
 * come first (alphabetical) and checked-off items sink to the bottom.
 */
object GroceryListOrganizer {

    fun organize(items: List<GroceryItem>, store: Store = Store.DEFAULT): List<SectionGroup> {
        return items
            .groupBy { it.section }
            .entries
            .sortedBy { sectionRank(store, it.key) }
            .map { (section, sectionItems) ->
                SectionGroup(
                    section,
                    sectionItems.sortedWith(
                        compareBy({ it.isChecked }, { it.name.lowercase() }),
                    ),
                )
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

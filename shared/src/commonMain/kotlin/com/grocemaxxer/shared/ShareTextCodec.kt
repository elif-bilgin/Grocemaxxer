package com.grocemaxxer.shared

/**
 * Turns a grocery list into a text message that is pleasant for a human to
 * read in SMS/iMessage AND can be parsed back losslessly by the app on the
 * other end:
 *
 * ```
 * 🧺 grocemaxxer list · Sat, Jul 18
 * == Produce ==
 * [ ] Apples
 * [x] Bananas
 * == Dairy & Eggs ==
 * [ ] Milk
 * ```
 */
object ShareTextCodec {

    private const val HEADER_MARKER = "grocemaxxer list"
    private val sectionHeaderRegex = Regex("^==\\s*(.+?)\\s*==$")
    private val itemLineRegex = Regex("^\\[( |x|X)\\]\\s*(.+)$")
    private val sectionsByDisplayName =
        StoreSection.entries.associateBy { it.displayName.lowercase() }

    fun encode(items: List<GroceryItem>, dateLabel: String): String {
        val groups = GroceryListOrganizer.organize(items)
        return buildString {
            append("🧺 grocemaxxer list · ").append(dateLabel)
            for (group in groups) {
                append("\n== ").append(group.section.displayName).append(" ==")
                for (item in group.items) {
                    append('\n')
                    append(if (item.isChecked) "[x] " else "[ ] ")
                    append(item.name)
                }
            }
        }
    }

    /**
     * Parses a shared message back into items, or returns null if [text]
     * isn't a grocemaxxer list at all. Unknown/missing section headers fall
     * back to keyword categorization, so hand-edited messages still import.
     */
    fun parse(text: String): List<GroceryItem>? {
        if (!text.lowercase().contains(HEADER_MARKER)) return null
        val items = mutableListOf<GroceryItem>()
        var currentSection: StoreSection? = null
        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            val sectionMatch = sectionHeaderRegex.find(line)
            if (sectionMatch != null) {
                currentSection = sectionsByDisplayName[sectionMatch.groupValues[1].lowercase()]
                continue
            }
            val itemMatch = itemLineRegex.find(line) ?: continue
            val name = itemMatch.groupValues[2].trim().toTitleCase()
            if (name.isEmpty()) continue
            items.add(
                GroceryItem(
                    id = "recv-${items.size}",
                    name = name,
                    section = currentSection ?: ItemCategorizer.categorize(name),
                    isChecked = itemMatch.groupValues[1].equals("x", ignoreCase = true),
                ),
            )
        }
        return items
    }

    data class ListDiff(
        val added: List<String>,
        val removed: List<String>,
        val checkChanged: List<String>,
    ) {
        val isIdentical: Boolean
            get() = added.isEmpty() && removed.isEmpty() && checkChanged.isEmpty()

        fun summary(): String {
            if (isIdentical) return "Lists match"
            val parts = mutableListOf<String>()
            if (added.isNotEmpty()) parts += "${added.size} new"
            if (removed.isNotEmpty()) parts += "${removed.size} missing"
            if (checkChanged.isNotEmpty()) parts += "${checkChanged.size} check-off change${if (checkChanged.size == 1) "" else "s"}"
            return parts.joinToString(" · ")
        }
    }

    /** What would change if [received] replaced [current]. */
    fun diff(current: List<GroceryItem>, received: List<GroceryItem>): ListDiff {
        val currentByKey = current.associateBy { it.name.toDedupeKey() }
        val receivedByKey = received.associateBy { it.name.toDedupeKey() }
        return ListDiff(
            added = received.filter { it.name.toDedupeKey() !in currentByKey }.map { it.name },
            removed = current.filter { it.name.toDedupeKey() !in receivedByKey }.map { it.name },
            checkChanged = received.filter { item ->
                val mine = currentByKey[item.name.toDedupeKey()]
                mine != null && mine.isChecked != item.isChecked
            }.map { it.name },
        )
    }
}

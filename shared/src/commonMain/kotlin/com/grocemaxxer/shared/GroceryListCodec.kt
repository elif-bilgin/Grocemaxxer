package com.grocemaxxer.shared

/**
 * Compact string codec for persisting the grocery list in a single
 * preferences value (used with DataStore). Fields are joined with ASCII
 * unit/record separators, which can't be typed into an item name from a
 * keyboard; they're stripped from names on encode as a belt-and-braces
 * measure anyway.
 */
object GroceryListCodec {

    private const val ITEM_SEP = '\u001E'
    private const val FIELD_SEP = '\u001F'

    fun encode(items: List<GroceryItem>): String =
        items.joinToString(ITEM_SEP.toString()) { item ->
            listOf(
                item.id.sanitized(),
                item.name.sanitized(),
                item.section.name,
                if (item.isChecked) "1" else "0",
            ).joinToString(FIELD_SEP.toString())
        }

    fun decode(encoded: String): List<GroceryItem> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(ITEM_SEP).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEP)
            if (fields.size < 4 || fields[0].isEmpty() || fields[1].isEmpty()) return@mapNotNull null
            GroceryItem(
                id = fields[0],
                name = fields[1],
                section = StoreSection.entries.firstOrNull { it.name == fields[2] } ?: StoreSection.OTHER,
                isChecked = fields[3] == "1",
            )
        }
    }

    private fun String.sanitized(): String = replace(ITEM_SEP, ' ').replace(FIELD_SEP, ' ')
}

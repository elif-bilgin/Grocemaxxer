package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareTextCodecTest {

    private fun item(name: String, checked: Boolean = false) = GroceryItem(
        id = name,
        name = name,
        section = ItemCategorizer.categorize(name),
        isChecked = checked,
    )

    @Test
    fun roundTripsNamesSectionsAndCheckedState() {
        val items = listOf(
            item("Apples"),
            item("Bananas", checked = true),
            item("Milk"),
            item("Paper Towels"),
        )

        val text = ShareTextCodec.encode(items, "Sat, Jul 18")
        val parsed = ShareTextCodec.parse(text)!!

        assertEquals(items.size, parsed.size)
        val byName = parsed.associateBy { it.name }
        assertEquals(StoreSection.PRODUCE, byName.getValue("Apples").section)
        assertTrue(byName.getValue("Bananas").isChecked)
        assertEquals(StoreSection.DAIRY_EGGS, byName.getValue("Milk").section)
        assertEquals(StoreSection.HOUSEHOLD, byName.getValue("Paper Towels").section)
        assertEquals(false, byName.getValue("Milk").isChecked)
    }

    @Test
    fun encodedTextIsHumanReadable() {
        val text = ShareTextCodec.encode(listOf(item("Apples"), item("Milk", checked = true)), "Sat, Jul 18")

        assertTrue(text.startsWith("🧺 grocemaxxer list · Sat, Jul 18"))
        assertTrue(text.contains("== Produce =="))
        assertTrue(text.contains("[ ] Apples"))
        assertTrue(text.contains("[x] Milk"))
    }

    @Test
    fun nonListTextReturnsNull() {
        assertNull(ShareTextCodec.parse("hey can you grab milk on the way home?"))
    }

    @Test
    fun handEditedMessageWithoutHeadersStillImports() {
        val text = """
            🧺 grocemaxxer list · whenever
            [ ] milk
            [x] apples
            [ ] mystery goo
        """.trimIndent()

        val parsed = ShareTextCodec.parse(text)!!

        assertEquals(3, parsed.size)
        assertEquals(StoreSection.DAIRY_EGGS, parsed[0].section)
        assertEquals("Milk", parsed[0].name)
        assertTrue(parsed[1].isChecked)
        assertEquals(StoreSection.OTHER, parsed[2].section)
    }

    @Test
    fun diffDetectsAddedRemovedAndCheckChanges() {
        val mine = listOf(item("Milk"), item("Apples"), item("Bread", checked = false))
        val received = listOf(item("Milk"), item("Eggs"), item("Bread", checked = true))

        val diff = ShareTextCodec.diff(mine, received)

        assertEquals(listOf("Eggs"), diff.added)
        assertEquals(listOf("Apples"), diff.removed)
        assertEquals(listOf("Bread"), diff.checkChanged)
        assertEquals(false, diff.isIdentical)
        assertEquals("1 new · 1 missing · 1 check-off change", diff.summary())
    }

    @Test
    fun identicalListsDiffAsIdentical() {
        val mine = listOf(item("Milk"), item("Apples", checked = true))
        val received = ShareTextCodec.parse(ShareTextCodec.encode(mine, "x"))!!

        assertTrue(ShareTextCodec.diff(mine, received).isIdentical)
    }
}

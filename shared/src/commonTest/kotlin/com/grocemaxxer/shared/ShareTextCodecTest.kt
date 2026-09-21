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

        assertTrue(text.startsWith("🧺 GroceMaxxer list · Sat, Jul 18"))
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

    @Test
    fun mergeAppliesOnlySelectedChoices() {
        val mine = listOf(
            item("Milk"),
            item("Apples"),
            item("Bread", checked = false),
            item("Cheese", checked = false),
        )
        val theirs = listOf(
            item("Milk"),
            item("Eggs"),
            item("Butter"),
            item("Bread", checked = true),
            item("Cheese", checked = true),
        )

        val merged = ShareTextCodec.merge(
            current = mine,
            received = theirs,
            acceptAdds = setOf("Eggs"), // take Eggs, skip Butter
            acceptRemovals = setOf("apples"), // drop Apples (case-insensitive)
            acceptChecks = setOf("Bread"), // take their Bread check, keep my Cheese state
        )

        val byName = merged.associateBy { it.name }
        assertEquals(setOf("Milk", "Bread", "Cheese", "Eggs"), byName.keys)
        assertTrue(byName.getValue("Bread").isChecked)
        assertEquals(false, byName.getValue("Cheese").isChecked)
    }

    @Test
    fun mergeWithNoSelectionsKeepsMyListUntouched() {
        val mine = listOf(item("Milk"), item("Apples", checked = true))
        val theirs = listOf(item("Eggs"), item("Milk", checked = true))

        val merged = ShareTextCodec.merge(mine, theirs, emptySet(), emptySet(), emptySet())

        assertEquals(mine, merged)
    }

    @Test
    fun mergeAcceptingEverythingMatchesOverwriteSemanticsForDiffedItems() {
        val mine = listOf(item("Milk"), item("Apples"))
        val theirs = listOf(item("Milk", checked = true), item("Eggs"))
        val diff = ShareTextCodec.diff(mine, theirs)

        val merged = ShareTextCodec.merge(
            current = mine,
            received = theirs,
            acceptAdds = diff.added.toSet(),
            acceptRemovals = diff.removed.toSet(),
            acceptChecks = diff.checkChanged.toSet(),
        )

        assertTrue(ShareTextCodec.diff(merged, theirs).isIdentical)
    }
}

package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class TextFormatTest {

    @Test
    fun titleCasesWordsAndNormalizesWhitespace() {
        assertEquals("Olive Oil", "olive OIL".toTitleCase())
        assertEquals("Milk", "  milk ".toTitleCase())
        assertEquals("Sweet Potato Fries", "sweet   potato fries".toTitleCase())
    }

    @Test
    fun filtersDuplicatesAgainstExistingAndWithinBatch() {
        val existing = listOf("Milk", "Olive Oil")
        val candidates = listOf("milk", "Eggs", "eggs", "  olive  oil ", "Bread")

        assertEquals(listOf("Eggs", "Bread"), filterNewNames(existing, candidates))
    }

    @Test
    fun dateLabelsFormatCorrectly() {
        assertEquals("Sat, Jul 18", shortDateLabel("2026-07-18"))
        assertEquals("Saturday, Jul 18", fullDateLabel("2026-07-18"))
        assertEquals("Sat, Jul 18, 2026", pickerDateLabel("2026-07-18"))
        assertEquals("September 20, 2026", longDateLabel("2026-09-20"))
        assertEquals("July 18, 2026", longDateLabel("2026-07-18"))
        assertEquals("not-a-date", shortDateLabel("not-a-date"))
    }
}

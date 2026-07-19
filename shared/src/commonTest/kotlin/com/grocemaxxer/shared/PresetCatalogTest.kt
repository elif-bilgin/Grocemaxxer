package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresetCatalogTest {

    @Test
    fun namesAreUniqueAndNonBlank() {
        val keys = PresetCatalog.items.map { it.name.toDedupeKey() }
        assertTrue(keys.none { it.isEmpty() })
        assertEquals(keys.size, keys.toSet().size, "duplicate catalog names")
    }

    @Test
    fun catalogIsComprehensive() {
        assertTrue(PresetCatalog.items.size >= 200, "expected a rich catalog, got ${PresetCatalog.items.size}")
    }

    @Test
    fun everyRealSectionHasPresets() {
        val covered = PresetCatalog.items.map { it.section }.toSet()
        val expected = StoreSection.entries.filter { it != StoreSection.OTHER }.toSet()
        assertEquals(expected, covered)
    }
}

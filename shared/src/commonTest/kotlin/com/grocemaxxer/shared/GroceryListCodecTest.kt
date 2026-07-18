package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroceryListCodecTest {

    @Test
    fun roundTripsItemsIncludingCheckedState() {
        val items = listOf(
            GroceryItem("1", "milk", StoreSection.DAIRY_EGGS, isChecked = false),
            GroceryItem("2", "sweet potatoes", StoreSection.PRODUCE, isChecked = true),
            GroceryItem("3", "quantum snack", StoreSection.OTHER, isChecked = false),
        )

        assertEquals(items, GroceryListCodec.decode(GroceryListCodec.encode(items)))
    }

    @Test
    fun emptyListRoundTrips() {
        assertEquals("", GroceryListCodec.encode(emptyList()))
        assertTrue(GroceryListCodec.decode("").isEmpty())
    }

    @Test
    fun unknownSectionNameFallsBackToOther() {
        val encoded = GroceryListCodec.encode(
            listOf(GroceryItem("1", "mystery", StoreSection.PRODUCE)),
        ).replace(StoreSection.PRODUCE.name, "NOT_A_REAL_SECTION")

        assertEquals(StoreSection.OTHER, GroceryListCodec.decode(encoded).single().section)
    }

    @Test
    fun malformedEntriesAreSkippedInsteadOfCrashing() {
        assertTrue(GroceryListCodec.decode("garbage-data-with-no-separators").isEmpty())
    }
}

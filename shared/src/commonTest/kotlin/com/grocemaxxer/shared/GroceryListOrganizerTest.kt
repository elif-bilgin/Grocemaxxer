package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroceryListOrganizerTest {

    private fun item(name: String) = GroceryItem(
        id = name,
        name = name,
        section = ItemCategorizer.categorize(name),
    )

    @Test
    fun groupsAndOrdersBySectionWalkOrder() {
        // Deliberately entered out of store order.
        val items = listOf(
            item("milk"),
            item("apple"),
            item("frozen pizza"),
            item("bread"),
            item("banana"),
            item("cheese"),
        )

        val groups = GroceryListOrganizer.organize(items)

        val sectionOrder = groups.map { it.section }
        assertEquals(sectionOrder.sortedBy { it.order }, sectionOrder, "sections must appear in store-walk order")
        assertEquals(
            listOf(StoreSection.PRODUCE, StoreSection.BAKERY, StoreSection.DAIRY_EGGS, StoreSection.FROZEN),
            sectionOrder,
        )
    }

    @Test
    fun eachSectionAppearsAtMostOnce() {
        val items = listOf(item("apple"), item("banana"), item("carrot"), item("spinach"))
        val groups = GroceryListOrganizer.organize(items)

        assertEquals(1, groups.size, "all produce items should collapse into a single section group")
        assertEquals(4, groups.first().items.size)
    }

    @Test
    fun itemsWithinASectionAreAlphabetical() {
        val items = listOf(item("banana"), item("apple"), item("cherry"))
        val groups = GroceryListOrganizer.organize(items)

        val names = groups.first().items.map { it.name }
        assertEquals(names.sortedBy { it.lowercase() }, names)
    }

    @Test
    fun emptyInputProducesEmptyChecklist() {
        assertTrue(GroceryListOrganizer.organize(emptyList()).isEmpty())
    }

    @Test
    fun inputParserSplitsOnCommasAndNewlinesAndTrims() {
        val parsed = GroceryInputParser.parse(" apples, banana \n  milk\n\nbread ,")
        assertEquals(listOf("apples", "banana", "milk", "bread"), parsed)
    }
}

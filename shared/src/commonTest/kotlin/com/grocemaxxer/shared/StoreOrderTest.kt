package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoreOrderTest {

    private fun item(name: String, checked: Boolean = false): GroceryItem =
        GroceryItem(id = name, name = name, section = ItemCategorizer.categorize(name), isChecked = checked)

    @Test
    fun safewayPutsPantryFirstAndBakeryDeliLast() {
        val items = listOf(item("bread"), item("apples"), item("pasta"), item("milk"), item("frozen pizza"))
        val sections = GroceryListOrganizer.organize(items, Store.SAFEWAY).map { it.section }

        assertEquals(StoreSection.PANTRY_CANNED, sections.first())
        assertEquals(StoreSection.BAKERY, sections.last())
        assertTrue(sections.indexOf(StoreSection.PRODUCE) < sections.indexOf(StoreSection.DAIRY_EGGS))
        assertTrue(sections.indexOf(StoreSection.DAIRY_EGGS) < sections.indexOf(StoreSection.FROZEN))
    }

    @Test
    fun traderJoesStartsWithProduce() {
        val items = listOf(item("pasta"), item("apples"), item("ice cream"))
        val sections = GroceryListOrganizer.organize(items, Store.TRADER_JOES).map { it.section }

        assertEquals(StoreSection.PRODUCE, sections.first())
        assertEquals(StoreSection.FROZEN, sections.last())
    }

    @Test
    fun costcoDoesDryGoodsFirstThenTheFreshWallThenFrozen() {
        val items = listOf(
            item("pasta"), item("apples"), item("chicken breast"),
            item("milk"), item("frozen pizza"), item("sourdough bread"),
        )
        val sections = GroceryListOrganizer.organize(items, Store.COSTCO).map { it.section }

        // Pallet aisles of dry goods are at the front of the warehouse.
        assertEquals(StoreSection.PANTRY_CANNED, sections.first())
        // Frozen bunkers are the last stop before the checkout runway.
        assertEquals(StoreSection.FROZEN, sections.last())
        // The fresh wall, loaded so nothing gets crushed: proteins, then
        // dairy, then produce on top, then the bakery.
        assertTrue(sections.indexOf(StoreSection.MEAT_SEAFOOD) < sections.indexOf(StoreSection.DAIRY_EGGS))
        assertTrue(sections.indexOf(StoreSection.DAIRY_EGGS) < sections.indexOf(StoreSection.PRODUCE))
        assertTrue(sections.indexOf(StoreSection.PRODUCE) < sections.indexOf(StoreSection.BAKERY))
    }

    @Test
    fun defaultOrderMatchesSectionWalkOrder() {
        val items = listOf(item("milk"), item("apples"), item("bread"), item("tulips"))
        val sections = GroceryListOrganizer.organize(items, Store.DEFAULT).map { it.section }

        assertEquals(
            listOf(StoreSection.FLORAL, StoreSection.PRODUCE, StoreSection.BAKERY, StoreSection.DAIRY_EGGS),
            sections,
        )
    }

    @Test
    fun everyStoreRanksEverySectionUniquely() {
        for (store in Store.entries) {
            val ranks = StoreSection.entries.map { sectionRank(store, it) }
            assertEquals(ranks.size, ranks.toSet().size, "duplicate ranks for $store")
        }
    }

    @Test
    fun checkedItemsSinkToBottomOfTheirSection() {
        val items = listOf(
            item("banana", checked = true),
            item("apple", checked = false),
            item("cherry", checked = false),
        )
        val group = GroceryListOrganizer.organize(items).single()

        assertEquals(listOf("apple", "cherry", "banana"), group.items.map { it.name })
    }
}

package com.grocemaxxer.shared

data class GroceryItem(
    val id: String,
    val name: String,
    val section: StoreSection,
    val isChecked: Boolean = false,
)

data class SectionGroup(
    val section: StoreSection,
    val items: List<GroceryItem>,
)

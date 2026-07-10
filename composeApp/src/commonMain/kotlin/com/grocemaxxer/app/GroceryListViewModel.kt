package com.grocemaxxer.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.grocemaxxer.shared.GroceryInputParser
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.GroceryListOrganizer
import com.grocemaxxer.shared.ItemCategorizer
import com.grocemaxxer.shared.SectionGroup

/** Plain Compose state holder -- no platform ViewModel dependency required. */
class GroceryListViewModel {
    private val items = mutableStateListOf<GroceryItem>()
    private var nextId = 0L

    var groups by mutableStateOf<List<SectionGroup>>(emptyList())
        private set

    val totalCount: Int get() = items.size
    val checkedCount: Int get() = items.count { it.isChecked }

    fun addItems(rawInput: String) {
        val names = GroceryInputParser.parse(rawInput)
        if (names.isEmpty()) return
        for (name in names) {
            items.add(
                GroceryItem(
                    id = (nextId++).toString(),
                    name = name,
                    section = ItemCategorizer.categorize(name),
                ),
            )
        }
        refresh()
    }

    fun toggleChecked(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) {
            items[index] = items[index].copy(isChecked = !items[index].isChecked)
        }
        refresh()
    }

    fun removeItem(id: String) {
        items.removeAll { it.id == id }
        refresh()
    }

    fun clearChecked() {
        items.removeAll { it.isChecked }
        refresh()
    }

    fun clearAll() {
        items.clear()
        refresh()
    }

    private fun refresh() {
        groups = GroceryListOrganizer.organize(items)
    }
}

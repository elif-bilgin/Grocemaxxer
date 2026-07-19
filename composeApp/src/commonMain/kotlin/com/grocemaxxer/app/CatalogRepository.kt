package com.grocemaxxer.app

import com.grocemaxxer.shared.CatalogItem
import com.grocemaxxer.shared.PresetCatalog
import com.grocemaxxer.shared.StoreSection
import com.grocemaxxer.shared.toDedupeKey

/**
 * Room-backed catalog of known grocery items ("pre-saved potential items").
 * Seeded from [PresetCatalog] on first launch. Deliberately static: typed
 * items are NOT remembered, so a one-off typo ("Eggz") can never haunt the
 * suggestions alongside "Egg" and "Eggs".
 */
class CatalogRepository(private val database: CatalogDatabase = catalogDatabase) {

    private val dao get() = database.catalogDao()

    suspend fun ensureSeeded() {
        // Purge anything recorded by older app versions that saved typed
        // items into the catalog (the source of misspelled suggestions).
        dao.deleteCustomItems()
        if (dao.count() == 0) {
            dao.insertAll(
                PresetCatalog.items.map {
                    CatalogItemEntity(name = it.name, sectionName = it.section.name, isCustom = false)
                },
            )
        }
    }

    /** Live-search suggestions for the add sheet, excluding items already on the list. */
    suspend fun suggestions(
        query: String,
        excludeNames: Collection<String>,
        limit: Int = 8,
    ): List<CatalogItem> {
        if (query.isBlank()) return emptyList()
        val excluded = excludeNames.map { it.toDedupeKey() }.toSet()
        return dao.search(query.trim(), limit + excluded.size)
            .map { it.toCatalogItem() }
            .filter { it.name.toDedupeKey() !in excluded }
            .take(limit)
    }

    suspend fun itemsFor(section: StoreSection): List<CatalogItem> =
        dao.bySection(section.name).map { it.toCatalogItem() }

    private fun CatalogItemEntity.toCatalogItem() = CatalogItem(
        name = name,
        section = StoreSection.entries.firstOrNull { it.name == sectionName } ?: StoreSection.OTHER,
    )
}

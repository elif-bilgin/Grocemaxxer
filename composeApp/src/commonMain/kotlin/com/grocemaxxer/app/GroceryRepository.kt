package com.grocemaxxer.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grocemaxxer.shared.GroceryInputParser
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.GroceryListCodec
import com.grocemaxxer.shared.ItemCategorizer
import com.grocemaxxer.shared.StoreSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import kotlin.random.Random

data class AppSettings(
    val palette: ThemePalette = ThemePalette.MINT,
    val darkMode: Boolean = false,
)

/** DataStore-backed store of the grocery list + user preferences. */
class GroceryRepository(private val dataStore: DataStore<Preferences>) {

    private val itemsKey = stringPreferencesKey("items")
    private val paletteKey = stringPreferencesKey("theme_palette")
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    private val safeData: Flow<Preferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    val items: Flow<List<GroceryItem>> = safeData.map { prefs ->
        GroceryListCodec.decode(prefs[itemsKey] ?: "")
    }

    val settings: Flow<AppSettings> = safeData.map { prefs ->
        AppSettings(
            palette = ThemePalette.fromName(prefs[paletteKey]),
            darkMode = prefs[darkModeKey] ?: false,
        )
    }

    suspend fun addItems(rawInput: String, manualSection: StoreSection? = null) {
        val names = GroceryInputParser.parse(rawInput)
        if (names.isEmpty()) return
        editItems { current ->
            current + names.map { name ->
                GroceryItem(
                    id = newId(),
                    name = name,
                    section = manualSection ?: ItemCategorizer.categorize(name),
                )
            }
        }
    }

    suspend fun toggleChecked(id: String) {
        editItems { current ->
            current.map { if (it.id == id) it.copy(isChecked = !it.isChecked) else it }
        }
    }

    suspend fun removeItem(id: String) {
        editItems { current -> current.filterNot { it.id == id } }
    }

    suspend fun clearChecked() {
        editItems { current -> current.filterNot { it.isChecked } }
    }

    suspend fun clearAll() {
        editItems { emptyList() }
    }

    suspend fun setPalette(palette: ThemePalette) {
        dataStore.edit { it[paletteKey] = palette.name }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[darkModeKey] = enabled }
    }

    private suspend fun editItems(transform: (List<GroceryItem>) -> List<GroceryItem>) {
        dataStore.edit { prefs ->
            val current = GroceryListCodec.decode(prefs[itemsKey] ?: "")
            prefs[itemsKey] = GroceryListCodec.encode(transform(current))
        }
    }

    private fun newId(): String = Random.nextLong().toString(16) + "-" + Random.nextInt(0xFFFF).toString(16)
}

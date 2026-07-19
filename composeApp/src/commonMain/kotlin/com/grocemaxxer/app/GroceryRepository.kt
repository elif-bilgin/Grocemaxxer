package com.grocemaxxer.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grocemaxxer.shared.GroceryInputParser
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.GroceryListCodec
import com.grocemaxxer.shared.ItemCategorizer
import com.grocemaxxer.shared.Store
import com.grocemaxxer.shared.StoreSection
import com.grocemaxxer.shared.filterNewNames
import com.grocemaxxer.shared.todayIsoDate
import com.grocemaxxer.shared.toTitleCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException
import kotlin.random.Random

data class AppSettings(
    val palette: ThemePalette = ThemePalette.MINT,
    val darkMode: Boolean = false,
    val store: Store = Store.DEFAULT,
)

/**
 * DataStore-backed store of grocery lists + user preferences.
 *
 * Lists are keyed by ISO date ("items@2026-07-18"), so past days' lists can
 * be browsed and re-used. All edits target today's list; adopting an older
 * list copies it onto today's date.
 */
class GroceryRepository(private val dataStore: DataStore<Preferences>) {

    val todayIso: String = todayIsoDate()

    private val datesKey = stringPreferencesKey("list_dates")
    private val paletteKey = stringPreferencesKey("theme_palette")
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val storeKey = stringPreferencesKey("default_store")
    private val accountEmailKey = stringPreferencesKey("account_email")
    private val accountNameKey = stringPreferencesKey("account_name")

    private fun itemsKey(isoDate: String) = stringPreferencesKey("items@$isoDate")

    private val safeData: Flow<Preferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    /** Today's list. */
    val items: Flow<List<GroceryItem>> = safeData.map { prefs ->
        GroceryListCodec.decode(prefs[itemsKey(todayIso)] ?: "")
    }

    val settings: Flow<AppSettings> = safeData.map { prefs ->
        AppSettings(
            palette = ThemePalette.fromName(prefs[paletteKey]),
            darkMode = prefs[darkModeKey] ?: false,
            store = Store.fromName(prefs[storeKey]),
        )
    }

    /** ISO dates that have a stored list, newest first. */
    val storedDates: Flow<List<String>> = safeData.map { prefs ->
        parseDates(prefs[datesKey]).sortedDescending()
    }

    /** Signed-in account, persisted across sessions until explicit logout. */
    val account: Flow<UserAccount?> = safeData.map { prefs ->
        prefs[accountEmailKey]?.let { email ->
            UserAccount(email = email, displayName = prefs[accountNameKey] ?: email)
        }
    }

    suspend fun setAccount(userAccount: UserAccount) {
        dataStore.edit { prefs ->
            prefs[accountEmailKey] = userAccount.email
            prefs[accountNameKey] = userAccount.displayName
        }
    }

    suspend fun clearAccount() {
        dataStore.edit { prefs ->
            prefs.remove(accountEmailKey)
            prefs.remove(accountNameKey)
        }
    }

    /** One-shot read of the list saved under [isoDate]. */
    suspend fun peekList(isoDate: String): List<GroceryItem> {
        val prefs = safeData.first()
        return GroceryListCodec.decode(prefs[itemsKey(isoDate)] ?: "")
    }

    /**
     * Copies the list stored under [fromIso] onto today's date (optionally
     * un-checking everything for a fresh run) and registers today.
     */
    suspend fun adoptList(fromIso: String, clearChecked: Boolean) {
        dataStore.edit { prefs ->
            val source = GroceryListCodec.decode(prefs[itemsKey(fromIso)] ?: "")
            val adopted = if (clearChecked) source.map { it.copy(isChecked = false) } else source
            prefs[itemsKey(todayIso)] = GroceryListCodec.encode(adopted)
            registerToday(prefs)
        }
    }

    /** Starts today from an empty list. */
    suspend fun startNewList() {
        dataStore.edit { prefs ->
            prefs[itemsKey(todayIso)] = ""
            registerToday(prefs)
        }
    }

    /**
     * Adds items with duplicate protection (case/whitespace-insensitive,
     * against the current list and within the batch) and Title Case names.
     */
    suspend fun addItems(rawInput: String, manualSection: StoreSection? = null) {
        val names = GroceryInputParser.parse(rawInput)
        if (names.isEmpty()) return
        editItems { current ->
            val newNames = filterNewNames(current.map { it.name }, names)
            current + newNames.map { name ->
                val displayName = name.toTitleCase()
                GroceryItem(
                    id = newId(),
                    name = displayName,
                    section = manualSection ?: ItemCategorizer.categorize(displayName),
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

    suspend fun setAllChecked(checked: Boolean) {
        editItems { current -> current.map { it.copy(isChecked = checked) } }
    }

    /** Replaces today's list wholesale (used when importing a shared list). */
    suspend fun replaceTodayList(newItems: List<GroceryItem>) {
        editItems { newItems.map { it.copy(id = newId()) } }
    }

    suspend fun setPalette(palette: ThemePalette) {
        dataStore.edit { it[paletteKey] = palette.name }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[darkModeKey] = enabled }
    }

    suspend fun setStore(store: Store) {
        dataStore.edit { it[storeKey] = store.name }
    }

    private suspend fun editItems(transform: (List<GroceryItem>) -> List<GroceryItem>) {
        dataStore.edit { prefs ->
            val current = GroceryListCodec.decode(prefs[itemsKey(todayIso)] ?: "")
            prefs[itemsKey(todayIso)] = GroceryListCodec.encode(transform(current))
            registerToday(prefs)
        }
    }

    private fun registerToday(prefs: MutablePreferences) {
        val dates = parseDates(prefs[datesKey])
        if (todayIso !in dates) {
            prefs[datesKey] = (dates + todayIso).joinToString(",")
        }
    }

    private fun parseDates(raw: String?): List<String> =
        raw?.split(',')?.filter { it.isNotBlank() } ?: emptyList()

    private fun newId(): String =
        Random.nextLong().toString(16) + "-" + Random.nextInt(0xFFFF).toString(16)
}

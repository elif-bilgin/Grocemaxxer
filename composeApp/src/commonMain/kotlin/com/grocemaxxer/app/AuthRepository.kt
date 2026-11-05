package com.grocemaxxer.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException

/**
 * Stores the signed-in session alongside the lists, in the same DataStore.
 *
 * The session is kept indefinitely with one exception: it is tied to the
 * version code it was issued under, so updating the app requires signing in
 * again. That is the requested policy, and it also gives a way to force
 * re-authentication if a release ever needs to.
 */
class AuthRepository(private val dataStore: DataStore<Preferences>) {

    private val emailKey = stringPreferencesKey("auth_email")
    private val tokenKey = stringPreferencesKey("auth_token")
    private val versionKey = intPreferencesKey("auth_version")

    private val safeData: Flow<Preferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    /**
     * The current session, or null when nobody is signed in -- including when
     * a session exists but was issued by an older build.
     */
    val session: Flow<AuthSession?> = safeData.map { prefs ->
        val email = prefs[emailKey] ?: return@map null
        val token = prefs[tokenKey] ?: return@map null
        val version = prefs[versionKey] ?: return@map null
        if (version != appVersionCode()) null else AuthSession(email, token, version)
    }

    suspend fun save(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[emailKey] = session.email
            prefs[tokenKey] = session.token
            prefs[versionKey] = session.issuedForVersion
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(emailKey)
            prefs.remove(tokenKey)
            prefs.remove(versionKey)
        }
    }
}

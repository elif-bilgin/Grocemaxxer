package com.grocemaxxer.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/** Absolute path of the preferences file; must end in `.preferences_pb`. */
expect fun groceryDataStorePath(): String

/** Process-wide singleton -- DataStore forbids two instances on one file. */
val groceryDataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { groceryDataStorePath().toPath() },
    )
}

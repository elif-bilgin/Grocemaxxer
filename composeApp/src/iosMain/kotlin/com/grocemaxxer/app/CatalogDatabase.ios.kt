package com.grocemaxxer.app

import androidx.room3.Room
import androidx.room3.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun catalogDatabaseBuilder(): RoomDatabase.Builder<CatalogDatabase> {
    val documents = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true,
    ).firstOrNull() as? String ?: ""
    return Room.databaseBuilder<CatalogDatabase>(
        name = "$documents/grocemaxxer_catalog.db",
    )
}

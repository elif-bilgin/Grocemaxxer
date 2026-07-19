package com.grocemaxxer.app

import androidx.room3.Room
import androidx.room3.RoomDatabase

actual fun catalogDatabaseBuilder(): RoomDatabase.Builder<CatalogDatabase> {
    val context = GrocemaxxerAndroidContext.appContext
    val dbFile = context.getDatabasePath("grocemaxxer_catalog.db")
    return Room.databaseBuilder<CatalogDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}

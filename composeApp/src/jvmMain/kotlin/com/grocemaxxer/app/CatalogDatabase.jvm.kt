package com.grocemaxxer.app

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

actual fun catalogDatabaseBuilder(): RoomDatabase.Builder<CatalogDatabase> {
    val dir = File(System.getProperty("user.home"), ".grocemaxxer")
    if (!dir.exists()) dir.mkdirs()
    return Room.databaseBuilder<CatalogDatabase>(
        name = File(dir, "grocemaxxer_catalog.db").absolutePath,
    )
}

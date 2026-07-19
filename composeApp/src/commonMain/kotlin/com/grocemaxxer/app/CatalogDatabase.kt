package com.grocemaxxer.app

import androidx.room3.ConstructedBy
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey val name: String,
    val sectionName: String,
    val isCustom: Boolean,
)

@Dao
interface CatalogDao {
    @Query(
        "SELECT * FROM catalog_items WHERE name LIKE '%' || :query || '%' " +
            "ORDER BY name LIMIT :limit",
    )
    suspend fun search(query: String, limit: Int): List<CatalogItemEntity>

    @Query("SELECT * FROM catalog_items WHERE sectionName = :sectionName ORDER BY name")
    suspend fun bySection(sectionName: String): List<CatalogItemEntity>

    @Query("SELECT COUNT(*) FROM catalog_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items WHERE isCustom = 1")
    suspend fun deleteCustomItems()
}

@Database(entities = [CatalogItemEntity::class], version = 1)
@ConstructedBy(CatalogDatabaseConstructor::class)
abstract class CatalogDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
}

// The Room compiler generates the `actual` implementations per platform.
@Suppress("KotlinNoActualForExpected", "NO_ACTUAL_FOR_EXPECT")
expect object CatalogDatabaseConstructor : RoomDatabaseConstructor<CatalogDatabase> {
    override fun initialize(): CatalogDatabase
}

/** Platform-specific builder pointing at the app's catalog.db location. */
expect fun catalogDatabaseBuilder(): RoomDatabase.Builder<CatalogDatabase>

/** Process-wide singleton catalog database. */
val catalogDatabase: CatalogDatabase by lazy {
    catalogDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

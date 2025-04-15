package com.isis3510.growhub.local.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {

    // Get categories ordered correctly, with limit and offset for pagination
    @Query("SELECT * FROM categories ORDER BY `order` ASC LIMIT :limit OFFSET :offset")
    suspend fun getCategories(limit: Int, offset: Int): List<CategoryEntity>

    // Insert a list of categories. Replace on conflict using the primary key (name).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    // Clear all categories, useful before inserting a fresh list from network
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
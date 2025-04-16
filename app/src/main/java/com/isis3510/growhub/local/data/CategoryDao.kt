package com.isis3510.growhub.local.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categoryentity ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getCategories(limit: Int, offset: Int): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM categoryentity")
    suspend fun getCount(): Int

    @Query("DELETE FROM categoryentity WHERE :now - createdAt > :maxAge")
    suspend fun deleteOlderThan(now: Long, maxAge: Long)
}
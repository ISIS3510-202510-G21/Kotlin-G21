package com.isis3510.growhub.local.data

import androidx.room.*

@Dao
interface EventDao {

    @Query("SELECT * FROM evententity ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getEvents(limit: Int, offset: Int): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT COUNT(*) FROM evententity")
    suspend fun getCount(): Int
}


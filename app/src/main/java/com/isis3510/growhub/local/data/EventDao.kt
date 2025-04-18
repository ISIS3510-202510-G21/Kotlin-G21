package com.isis3510.growhub.local.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    suspend fun getEvents(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT * FROM events WHERE name LIKE :query OR description LIKE :query")
    suspend fun searchEvents(query: String): List<EventEntity>

    @Query("DELETE FROM events")
    suspend fun clearEvents()


}
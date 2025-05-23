package com.isis3510.growhub.local.data

import androidx.room.*

@Dao
interface EventDao {

    @Query("SELECT * FROM evententity ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getEvents(limit: Int, offset: Int): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT COUNT(*) FROM evententity")
    suspend fun getCount(): Int

    @Query("SELECT * FROM evententity WHERE cost = 0")
    suspend fun getFreeEvents(): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE locationLatitude BETWEEN :latMin AND :latMax AND locationLongitude BETWEEN :lonMin AND :lonMax")
    suspend fun getNearbyEvents(latMin: Double, latMax: Double, lonMin: Double, lonMax: Double): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE startDate >= :today ORDER BY startDate ASC")
    suspend fun getUpcomingEvents(today: String): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE startDate < :today ORDER BY startDate DESC")
    suspend fun getPreviousEvents(today: String): List<EventEntity>

    @Query("DELETE FROM evententity WHERE id NOT IN (SELECT MIN(id) FROM evententity GROUP BY name)")
    suspend fun deleteDuplicates()

    @Query("SELECT * FROM evententity")
    suspend fun getAllLocalEvents(): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE name = :name")
    suspend fun getEventByName(name: String): EventEntity
}




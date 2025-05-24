package com.isis3510.growhub.local.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profileentity")
    suspend fun getProfile(): ProfileEntity?

    @Query("SELECT * FROM profileentity WHERE name IN (:names)")
    suspend fun getProfilesByName(names: List<String>): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profiles: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)

    @Query("DELETE FROM profileentity WHERE id NOT IN (SELECT MIN(id) FROM profileentity GROUP BY name)")
    suspend fun deleteDuplicates()
}
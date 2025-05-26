package com.isis3510.growhub.local.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowItemDao {
    // **Followers**
    @Query("SELECT * FROM followers")
    fun getAllFollowers(): Flow<List<FollowerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowers(items: List<FollowerEntity>)

    @Query("DELETE FROM followers")
    suspend fun deleteAllFollowers()

    // **Following**
    @Query("SELECT * FROM following")
    fun getAllFollowing(): Flow<List<FollowingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowing(items: List<FollowingEntity>)

    @Query("DELETE FROM following")
    suspend fun deleteAllFollowing()
}

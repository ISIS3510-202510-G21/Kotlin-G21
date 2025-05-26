package com.isis3510.growhub.Repository

import com.isis3510.growhub.local.data.FollowerEntity
import com.isis3510.growhub.local.data.FollowingEntity
import com.isis3510.growhub.local.database.FollowLocalStore
import com.isis3510.growhub.model.objects.FollowItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FollowItemRepository(db: FollowLocalStore) {

    private val dao = db.followItemDao()

    /** Followers */
    fun getAllFollowers(): Flow<List<FollowItem>> =
        dao.getAllFollowers().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun insertFollowers(items: List<FollowItem>) {
        dao.deleteAllFollowers()
        dao.insertFollowers(items.map { FollowerEntity.fromDomain(it) })
    }

    /** Following */
    fun getAllFollowing(): Flow<List<FollowItem>> =
        dao.getAllFollowing().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun insertFollowing(items: List<FollowItem>) {
        dao.deleteAllFollowing()
        dao.insertFollowing(items.map { FollowingEntity.fromDomain(it) })
    }
}
package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isis3510.growhub.model.objects.FollowItem

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val headline: String,
    val profilePicture: String
) {
    fun toDomain() = FollowItem(userId, name, headline, profilePicture)
    companion object {
        fun fromDomain(item: FollowItem) = FollowerEntity(
            userId = item.userId,
            name = item.name,
            headline = item.headline,
            profilePicture = item.profilePicture
        )
    }
}

@Entity(tableName = "following")
data class FollowingEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val headline: String,
    val profilePicture: String
) {
    fun toDomain() = FollowItem(userId, name, headline, profilePicture)
    companion object {
        fun fromDomain(item: FollowItem) = FollowingEntity(
            userId = item.userId,
            name = item.name,
            headline = item.headline,
            profilePicture = item.profilePicture
        )
    }
}

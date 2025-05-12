package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profileentity")
data class ProfileEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val following: Int = 0,
    val followers: Int = 0,
    val description: String,
    val interests: List<String> = emptyList(),
    val profilePicture: String
)
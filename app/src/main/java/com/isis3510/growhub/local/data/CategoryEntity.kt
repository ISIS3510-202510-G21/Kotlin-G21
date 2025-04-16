package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categoryentity")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
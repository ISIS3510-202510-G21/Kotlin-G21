package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "evententity", indices = [Index(value = ["name", "startDate"], unique = true)])
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val locationInfo: String,
    val locationLatitude: Double,
    val locationLongitude: Double,
    val startDate: String,
    val endDate: String,
    val category: String,
    val imageUrl: String,
    val cost: Int,
    val attendees: List<String>,
    val skills: List<String>,
    val creator: String
)

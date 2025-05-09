package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evententity")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val locationId: String,
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

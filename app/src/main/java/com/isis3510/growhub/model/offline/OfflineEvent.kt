package com.isis3510.growhub.offline

import com.google.firebase.Timestamp

data class OfflineEvent(
    val name: String,
    val cost: Double,
    val category: String,
    val description: String,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val locationId: String,
    val imageUrl: String?,
    val address: String,
    val details: String,
    val city: String,
    val isUniversity: Boolean,
    val skillIds: List<String>
)

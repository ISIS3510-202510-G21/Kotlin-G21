package com.isis3510.growhub.model.objects

data class Event(
    val name: String,
    val description: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val category: String,
    val imageUrl: String,
    val cost: Int,
    val attendees: List<String>,
    val skills: List<String>,
    val creator: String
)
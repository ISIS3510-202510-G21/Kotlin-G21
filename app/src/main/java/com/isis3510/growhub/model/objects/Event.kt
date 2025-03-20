package com.isis3510.growhub.model.objects

data class Event(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val category: String,
    val imageUrl: String,
    val cost: Double,
)
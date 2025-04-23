package com.isis3510.growhub.model.objects

import com.isis3510.growhub.local.data.EventEntity

data class Event(
    val id: String,
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

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        name = name,
        description = description,
        location = location,
        startDate = startDate,
        endDate = endDate,
        category = category,
        imageUrl = imageUrl,
        cost = cost,
        attendees = attendees,
        skills = skills,
        creator = creator
    )
}

fun EventEntity.toModel(): Event {
    return Event(
        id = id,
        name = name,
        description = description,
        location = location,
        startDate = startDate,
        endDate = endDate,
        category = category,
        imageUrl = imageUrl,
        cost = cost,
        attendees = attendees,
        skills = skills,
        creator = creator
    )
}
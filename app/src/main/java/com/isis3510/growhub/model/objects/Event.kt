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

// Extension to convert between model entity and data entity
fun EventEntity.toEvent(): Event {
    return Event(
        id = this.id.toString(),
        name = this.name,
        description = this.description,
        location = this.location,
        startDate = this.startDate,
        endDate = this.endDate,
        category = this.category,
        imageUrl = this.imageUrl,
        cost = this.cost,
        attendees = this.attendees,
        skills = this.skills,
        creator = this.creator
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = 0,
        name = this.name,
        description = this.description,
        location = this.location,
        startDate = this.startDate,
        endDate = this.endDate,
        category = this.category,
        imageUrl = this.imageUrl,
        cost = this.cost,
        attendees = this.attendees,
        skills = this.skills,
        creator = this.creator
    )
}
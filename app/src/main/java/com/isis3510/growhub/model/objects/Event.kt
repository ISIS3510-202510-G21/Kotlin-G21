package com.isis3510.growhub.model.objects

import com.isis3510.growhub.local.data.EventEntity

data class Event(
    val name: String,
    val description: String,
    val location: Location,
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
        name = name,
        description = description,
        locationInfo = location.getInfo(),
        locationLatitude = location.latitude,
        locationLongitude = location.longitude,
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
        name = name,
        description = description,
        location = Location(address = locationInfo, latitude = locationLatitude, longitude = locationLongitude),
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

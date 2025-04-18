package com.isis3510.growhub.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isis3510.growhub.model.objects.Event

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
) {
    fun toEvent(): Event {
        return Event(
            id = id.toString(),
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

    companion object {
        fun fromEvent(event: Event): EventEntity {
            return EventEntity (
                id = event.id.toInt(),
                name = event.name,
                description = event.description,
                location = event.location,
                startDate = event.startDate,
                endDate = event.endDate,
                category = event.category,
                imageUrl = event.imageUrl,
                cost = event.cost,
                attendees = event.attendees,
                skills = event.skills,
                creator = event.creator
            )
        }
    }
}

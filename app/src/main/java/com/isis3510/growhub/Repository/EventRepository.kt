package com.isis3510.growhub.Repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.toEntity
import com.isis3510.growhub.model.objects.toModel

class EventRepository(
    db: AppLocalDatabase
) {

    private val eventDao = db.eventDao()

    // Load paginated events from local DB
    private suspend fun getEventsLocal(limit: Int, offset: Int): List<Event> {
        return eventDao.getEvents(limit, offset).map { it.toModel() }
    }

    // Load from cache first, fallback to Firebase
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getEvents(limit: Int, offset: Int): List<Event> {
        val localEvents = getEventsLocal(limit, offset)
        return localEvents
    }

    suspend fun deleteDuplicates() {
        eventDao.deleteDuplicates()
    }

    suspend fun getFreeEvents(): List<Event> {
        return eventDao.getFreeEvents().map { it.toModel() }
    }

    suspend fun getUpcomingEvents(today: String): List<Event> {
        return eventDao.getUpcomingEvents(today).map { it.toModel() }
    }

    suspend fun getPreviousEvents(today: String): List<Event> {
        return eventDao.getPreviousEvents(today).map { it.toModel() }
    }

    suspend fun getAllLocalEvents(): List<Event> {
        return eventDao.getAllLocalEvents().map { it.toModel() }
    }

    // Store locally
    suspend fun storeEvents(events: List<Event>) {
        val entities = events.map { it.toEntity() }
        eventDao.insertEvents(entities)
    }

    // Obtener un evento específico por ID
    suspend fun getEventById(eventId: Int): Event {
    return eventDao.getEventById(eventId).toModel() 
}

}


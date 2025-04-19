package com.isis3510.growhub.Repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.toEntity

class EventRepository(db: AppLocalDatabase) {

    private val eventDao = db.eventDao()

    suspend fun getLocalEvents(): List<Event> {
        return eventDao.getEvents().map { it.toEvent() }
    }

    suspend fun saveEventsLocally(events: List<Event>) {
        val entities = events.map { it.toEntity() }
        eventDao.insertEvents(entities)
    }
}




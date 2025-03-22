package com.isis3510.growhub.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
class MyEventsViewModel(
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    val upcomingEvents = mutableStateListOf<Event>()
    val previousEvents = mutableStateListOf<Event>()

    init {
        loadEventsFromFirebase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadEventsFromFirebase() {
        viewModelScope.launch {
            val events = firebaseFacade.fetchMyEvents()

            for (event in events) {
                val startDate = event.startDate
                val today = LocalDate.now()

                val eventDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                if (eventDate.isAfter(today)) {
                    upcomingEvents.add(event)
                } else {
                    previousEvents.add(event)
                }
            }
        }
    }
}
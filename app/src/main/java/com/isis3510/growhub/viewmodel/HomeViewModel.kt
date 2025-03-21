package com.isis3510.growhub.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade(),
) : ViewModel() {
    val upcomingEvents = mutableStateListOf<Event>()
    val nearbyEvents = mutableStateListOf<Event>()
    val recommendedEvents = mutableStateListOf<Event>()
    val categories = mutableStateListOf<Category>()

    init {
        loadUpcomingEventsFromFirebase()
        loadEventsFromFirebase()
        loadCategoriesFromFirebase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUpcomingEventsFromFirebase() {
        viewModelScope.launch {
            val events = firebaseFacade.fetchMyEvents()

            for (event in events) {
                val startDate = event.startDate
                val today = LocalDate.now()

                val eventDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                if (eventDate.isAfter(today)) {
                    upcomingEvents.add(event)
                }
            }
        }
    }

    private fun loadEventsFromFirebase() {
        viewModelScope.launch {
            val events = firebaseFacade.fetchHomeEvents()

            // Add only the first 3 events to the nearbyEvents and recommendedEvents list
            for (i in 0 until minOf(3, events.size)) {
                nearbyEvents.add(events[i])
            }

            for (i in 0 until minOf(3, events.size)) {
                recommendedEvents.add(events[i])
            }
        }
    }

    private fun loadCategoriesFromFirebase() {
        viewModelScope.launch {
            val categories = firebaseFacade.fetchCategories()
            this@HomeViewModel.categories.addAll(categories)
        }
    }
}

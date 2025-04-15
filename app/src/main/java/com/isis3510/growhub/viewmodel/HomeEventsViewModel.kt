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
import kotlin.math.min // Import min directly

@RequiresApi(Build.VERSION_CODES.O) // Still needed due to LocalDate usage
class HomeEventsViewModel(
    // Consider using Dependency Injection here in a real app (Hilt, Koin)
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    // State holders for different event lists
    val upcomingEvents = mutableStateListOf<Event>()
    val nearbyEvents = mutableStateListOf<Event>()
    val recommendedEvents = mutableStateListOf<Event>()

    // Consider using StateFlow for better integration with Compose recomposition
    // Example:
    // private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    // val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents.asStateFlow()
    // ... and update the loading functions accordingly

    init {
        // Load all event types on initialization
        loadUpcomingEvents()
        loadNearbyEvents()
        loadRecommendedEvents()
    }

    @RequiresApi(Build.VERSION_CODES.O) // Specifically for LocalDate usage
    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            try {
                val allMyEvents = firebaseFacade.fetchMyEvents()
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                val filteredUpcoming = allMyEvents.filter { event ->
                    try {
                        val eventDate = LocalDate.parse(event.startDate, formatter)
                        eventDate.isAfter(today)
                    } catch (e: Exception) {
                        // Handle potential parsing errors if format is inconsistent
                        // Log.e("EventsViewModel", "Error parsing date for event ${event.id}", e)
                        false // Exclude if date parsing fails
                    }
                }
                // Update the state list (clear first if necessary)
                upcomingEvents.clear()
                upcomingEvents.addAll(filteredUpcoming)

            } catch (e: Exception) {
                // Handle exceptions during Firebase fetch (e.g., network error)
                // Log.e("EventsViewModel", "Error fetching upcoming events", e)
                // You might want to expose an error state to the UI
            }
        }
    }

    private fun loadNearbyEvents() {
        viewModelScope.launch {
            try {
                val homeEvents = firebaseFacade.fetchHomeEvents()
                // Take only the first 3 (or fewer if list is smaller)
                val nearbySubset = homeEvents.take(min(3, homeEvents.size))

                nearbyEvents.clear()
                nearbyEvents.addAll(nearbySubset)

            } catch (e: Exception) {
                // Handle exceptions
                // Log.e("EventsViewModel", "Error fetching nearby events", e)
            }
        }
    }

    private fun loadRecommendedEvents() {
        viewModelScope.launch {
            try {
                val recommended = firebaseFacade.fetchHomeRecommendedEvents()
                // Take only the first 3 (or fewer if list is smaller)
                val recommendedSubset = recommended.take(min(3, recommended.size))

                recommendedEvents.clear()
                recommendedEvents.addAll(recommendedSubset)

            } catch (e: Exception) {
                // Handle exceptions
                // Log.e("EventsViewModel", "Error fetching recommended events", e)
            }
        }
    }

    // Potential future function for refreshing data
    // fun refreshEvents() {
    //     loadUpcomingEvents()
    //     loadNearbyEvents()
    //     loadRecommendedEvents()
    // }
}
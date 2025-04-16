package com.isis3510.growhub.viewmodel

import android.os.Build
import android.util.Log // Import Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
class HomeEventsViewModel(
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    // --- State Holders de Eventos ---
    val upcomingEvents = mutableStateListOf<Event>()
    val nearbyEvents = mutableStateListOf<Event>()
    val recommendedEvents = mutableStateListOf<Event>()

    // --- Estados de carga individuales ---
    private val _isLoadingUpcoming = mutableStateOf(true)
    val isLoadingUpcoming: State<Boolean> = _isLoadingUpcoming

    private val _isLoadingNearby = mutableStateOf(true)
    val isLoadingNearby: State<Boolean> = _isLoadingNearby

    private val _isLoadingRecommended = mutableStateOf(true)
    val isLoadingRecommended: State<Boolean> = _isLoadingRecommended

    // --- Inicialización ---
    init {
        loadAllEvents()
    }

    // --- Lógica de carga concurrente con estados independientes ---
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadAllEvents() {
        // Se podría quitar el flag global si se utiliza solo el de cada sección.
        viewModelScope.launch {
            try {
                // Lanzamos las tres peticiones concurrentemente
                val deferredUpcoming = async { firebaseFacade.fetchMyEvents() }
                val deferredNearby = async { firebaseFacade.fetchHomeEvents() }
                val deferredRecommended = async { firebaseFacade.fetchHomeRecommendedEvents() }

                // --- Upcoming Events ---
                runCatching { deferredUpcoming.await() }
                    .onSuccess { allMyEvents ->
                        processUpcomingEvents(allMyEvents)
                    }.onFailure { e ->
                        Log.e("HomeEventsVM", "Error fetching upcoming events", e)
                        upcomingEvents.clear()
                    }
                _isLoadingUpcoming.value = false

                // --- Nearby Events ---
                runCatching { deferredNearby.await() }
                    .onSuccess { homeEvents ->
                        processNearbyEvents(homeEvents)
                    }.onFailure { e ->
                        Log.e("HomeEventsVM", "Error fetching nearby events", e)
                        nearbyEvents.clear()
                    }
                _isLoadingNearby.value = false

                // --- Recommended Events ---
                runCatching { deferredRecommended.await() }
                    .onSuccess { recommended ->
                        processRecommendedEvents(recommended)
                    }.onFailure { e ->
                        Log.e("HomeEventsVM", "Error fetching recommended events", e)
                        recommendedEvents.clear()
                    }
                _isLoadingRecommended.value = false

            } catch (e: Exception) {
                Log.e("HomeEventsVM", "Error in loadAllEvents coroutine scope", e)
                upcomingEvents.clear()
                nearbyEvents.clear()
                recommendedEvents.clear()

                _isLoadingUpcoming.value = false
                _isLoadingNearby.value = false
                _isLoadingRecommended.value = false
            }
        }
    }

    // --- Funciones de procesamiento ---
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processUpcomingEvents(allMyEvents: List<Event>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val filteredUpcoming = allMyEvents.filter { event ->
            try {
                if (event.startDate.isEmpty()) return@filter false
                val eventDate = LocalDate.parse(event.startDate, formatter)
                eventDate.isAfter(today) || eventDate.isEqual(today)
            } catch (e: Exception) {
                Log.w("HomeEventsVM", "Error parsing date '${event.startDate}' for event ${event.name}. Skipping.", e)
                false
            }
        }
        val upcomingSubset = filteredUpcoming.take(3) // Limita a 3 eventos
        Log.d("HomeEventsVM", "Processed Upcoming: Found ${upcomingSubset.size} events.")
        upcomingEvents.clear()
        upcomingEvents.addAll(upcomingSubset)
    }

    private fun processNearbyEvents(homeEvents: List<Event>) {
        val nearbySubset = homeEvents.take(min(3, homeEvents.size))
        Log.d("HomeEventsVM", "Processed Nearby: Found ${nearbySubset.size} events.")
        nearbyEvents.clear()
        nearbyEvents.addAll(nearbySubset)
    }

    private fun processRecommendedEvents(recommended: List<Event>) {
        val recommendedSubset = recommended.take(min(3, recommended.size))
        Log.d("HomeEventsVM", "Processed Recommended: Found ${recommendedSubset.size} events.")
        recommendedEvents.clear()
        recommendedEvents.addAll(recommendedSubset)
    }
}

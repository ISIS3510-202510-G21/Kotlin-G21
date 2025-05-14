package com.isis3510.growhub.viewmodel

import android.app.Application
import android.util.Log
import androidx.collection.LruCache
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.data.GlobalData
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.utils.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.sqrt

class HomeEventsViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseServicesFacade = FirebaseServicesFacade()
    private val db = AppLocalDatabase.getDatabase(application)
    private val eventRepository = EventRepository(db)

    val upcomingEvents = mutableStateOf<List<Event>>(emptyList())
    val recommendedEvents = mutableStateOf<List<Event>>(emptyList())
    val nearbyEvents = mutableStateOf<List<Event>>(emptyList())

    val isLoadingUpcoming = mutableStateOf(false)
    val isLoadingRecommended = mutableStateOf(false)
    val isLoadingNearby = mutableStateOf(false)

    val isLoadingMoreUpcoming = mutableStateOf(false)
    val isLoadingMoreRecommended = mutableStateOf(false)
    val isLoadingMoreNearby = mutableStateOf(false)

    val hasReachedEndUpcoming = mutableStateOf(false)
    val hasReachedEndRecommended = mutableStateOf(false)
    val hasReachedEndNearby = mutableStateOf(false)

    private val connectivityViewModel = ConnectivityViewModel(application)
    private val _isOffline = mutableStateOf(false)
    val isOffline = _isOffline

    private var lastHomeUpcomingSnapshot: DocumentSnapshot? = null
    private var lastHomeNearbySnapshot: DocumentSnapshot? = null
    private var currentRecommendedIds = mutableSetOf<String>()

    // Use separate caches for each type of event
    private val upcomingCache = LruCache<String, List<Event>>(5)
    private val recommendedCache = LruCache<String, List<Event>>(5)
    private val nearbyCache = LruCache<String, List<Event>>(5)

    private val UPCOMING_KEY = "upcoming_events"
    private val RECOMMENDED_KEY = "recommended_events"
    private val NEARBY_KEY = "nearby_events"

    init {
        viewModelScope.launch {
            connectivityViewModel.networkStatus.collectLatest { status ->
                _isOffline.value = status == ConnectionStatus.Unavailable || status == ConnectionStatus.Lost
                loadInitialHomeEvents()
                Log.d("HomeEventsViewModel", "Network status: $status, isOffline=${_isOffline.value}")
            }
        }
    }

    private fun loadInitialHomeEvents() {
        loadInitialUpcomingEvents()
        loadInitialRecommendedEvents()
        loadInitialNearbyEvents()
    }

    // Upcoming
    private fun loadInitialUpcomingEvents() {
        isLoadingUpcoming.value = true

        // Solo cargamos desde cache/Room si hay datos disponibles o si estamos offline
        val cached = upcomingCache.get(UPCOMING_KEY)
        if (!cached.isNullOrEmpty()) {
            upcomingEvents.value = cached
            GlobalData.upcomingEvents = cached
            hasReachedEndUpcoming.value = false
        }

        if (_isOffline.value) {
            // En modo offline, obtenemos datos de Room si no había en caché
            if (cached.isNullOrEmpty()) {
                viewModelScope.launch {
                    try {
                        val room = eventRepository.getEvents(5, 0).filterUpcoming()
                        if (room.isNotEmpty()) {
                            upcomingEvents.value = room
                            GlobalData.upcomingEvents = room
                            upcomingCache.put(UPCOMING_KEY, room)
                        }
                        hasReachedEndUpcoming.value = room.isEmpty()
                    } catch (e: Exception) {
                        Log.e("HomeEventsViewModel", "Error loading upcoming events from Room: ${e.message}")
                    } finally {
                        isLoadingUpcoming.value = false
                    }
                }
            } else {
                // Ya habíamos cargado de caché, así que terminamos
                isLoadingUpcoming.value = false
            }
            return
        }

        // Modo online: cargamos de Firebase
        viewModelScope.launch {
            try {
                val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
                val filtered = events.filterUpcoming()

                // Store new events
                eventRepository.storeEvents(filtered)

                // Update GlobalData
                for (event in filtered) {
                    if (!GlobalData.allEvents.contains(event)) {
                        GlobalData.allEvents.add(event)
                    }
                }

                // Update cache with the new data
                upcomingCache.put(UPCOMING_KEY, filtered)

                // Merge with existing data to avoid overwriting
                val existingIds = upcomingEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                val newEvents = filtered.filterNot { "${it.name}-${it.startDate}" in existingIds }

                if (newEvents.isNotEmpty()) {
                    upcomingEvents.value += newEvents
                } else if (upcomingEvents.value.isEmpty()) {
                    upcomingEvents.value = filtered
                }

                GlobalData.upcomingEvents = upcomingEvents.value
                lastHomeUpcomingSnapshot = snapshot
                hasReachedEndUpcoming.value = filtered.isEmpty()
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error fetching upcoming events: ${e.message}")
                // Si fallamos online, pero aún no tenemos datos, intentamos obtenerlos de Room
                if (upcomingEvents.value.isEmpty()) {
                    try {
                        val room = eventRepository.getEvents(5, 0).filterUpcoming()
                        if (room.isNotEmpty()) {
                            upcomingEvents.value = room
                            GlobalData.upcomingEvents = room
                            upcomingCache.put(UPCOMING_KEY, room)
                        }
                        hasReachedEndUpcoming.value = room.isEmpty()
                    } catch (e: Exception) {
                        Log.e("HomeEventsViewModel", "Error loading upcoming from Room after Firebase failure: ${e.message}")
                    }
                }
            } finally {
                isLoadingUpcoming.value = false
            }
        }
    }

    fun loadMoreUpcomingEvents() {
        if (isLoadingMoreUpcoming.value || hasReachedEndUpcoming.value) return

        if (_isOffline.value) {
            // En modo offline, intentamos cargar más de Room
            isLoadingMoreUpcoming.value = true
            viewModelScope.launch {
                try {
                    val currentSize = upcomingEvents.value.size
                    val moreEvents = eventRepository.getEvents(5, currentSize).filterUpcoming()

                    if (moreEvents.isNotEmpty()) {
                        val existingIds = upcomingEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                        val newEvents = moreEvents.filterNot { "${it.name}-${it.startDate}" in existingIds }

                        if (newEvents.isNotEmpty()) {
                            upcomingEvents.value = upcomingEvents.value + newEvents
                            GlobalData.upcomingEvents = upcomingEvents.value
                            upcomingCache.put(UPCOMING_KEY, upcomingEvents.value)
                        } else {
                            hasReachedEndUpcoming.value = true
                        }
                    } else {
                        hasReachedEndUpcoming.value = true
                    }
                } catch (e: Exception) {
                    Log.e("HomeEventsViewModel", "Error loading more upcoming events offline: ${e.message}")
                } finally {
                    isLoadingMoreUpcoming.value = false
                }
            }
            return
        }

        isLoadingMoreUpcoming.value = true
        viewModelScope.launch {
            try {
                val (next, newSnap) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeUpcomingSnapshot)
                val filteredNext = next.filterUpcoming()

                if (filteredNext.isNotEmpty()) {
                    // Filter out duplicates before adding
                    val existingIds = upcomingEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                    val newEvents = filteredNext.filterNot { "${it.name}-${it.startDate}" in existingIds }

                    if (newEvents.isNotEmpty()) {
                        upcomingEvents.value += newEvents
                        GlobalData.upcomingEvents = upcomingEvents.value

                        // Add to GlobalData.allEvents
                        for (event in newEvents) {
                            if (!GlobalData.allEvents.contains(event)) {
                                GlobalData.allEvents.add(event)
                            }
                        }

                        // Store in database
                        eventRepository.storeEvents(newEvents)

                        // Update cache
                        upcomingCache.put(UPCOMING_KEY, upcomingEvents.value)
                        lastHomeUpcomingSnapshot = newSnap
                    } else {
                        hasReachedEndUpcoming.value = true
                    }
                } else {
                    hasReachedEndUpcoming.value = true
                }
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error loading more upcoming events: ${e.message}")
            } finally {
                isLoadingMoreUpcoming.value = false
            }
        }
    }

    // Recommended
    private fun loadInitialRecommendedEvents() {
        isLoadingRecommended.value = true

        // Primero verificamos la caché
        val cached = recommendedCache.get(RECOMMENDED_KEY)
        if (!cached.isNullOrEmpty()) {
            recommendedEvents.value = cached
            hasReachedEndRecommended.value = false
        }

        // Si estamos offline, terminamos con lo que teníamos en caché
        if (_isOffline.value) {
            // Si no teníamos nada en caché, tratamos de mostrar algo de la base de datos local
            if (cached.isNullOrEmpty()) {
                viewModelScope.launch {
                    try {
                        // Como alternativa, mostramos algunos eventos locales
                        val localEvents = eventRepository.getAllLocalEvents().take(5)
                        if (localEvents.isNotEmpty()) {
                            recommendedEvents.value = localEvents
                            recommendedCache.put(RECOMMENDED_KEY, localEvents)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeEventsViewModel", "Error loading recommended events offline: ${e.message}")
                    } finally {
                        isLoadingRecommended.value = false
                    }
                }
            } else {
                isLoadingRecommended.value = false
            }
            return
        }

        // Modo online: cargamos de Firebase
        viewModelScope.launch {
            try {
                val (events) = firebaseServicesFacade.fetchHomeRecommendedEvents()

                // Update ids set
                currentRecommendedIds = events.mapTo(mutableSetOf()) { it.name }

                // Update cache
                recommendedCache.put(RECOMMENDED_KEY, events)

                // Merge with existing data
                val existingIds = recommendedEvents.value.map { it.name }.toSet()
                val newEvents = events.filterNot { it.name in existingIds }

                if (newEvents.isNotEmpty()) {
                    recommendedEvents.value += newEvents
                } else if (recommendedEvents.value.isEmpty()) {
                    recommendedEvents.value = events
                }

                // Store in GlobalData
                for (event in events) {
                    if (!GlobalData.allEvents.contains(event)) {
                        GlobalData.allEvents.add(event)
                    }
                }

                hasReachedEndRecommended.value = events.isEmpty()
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error fetching recommended events: ${e.message}")
                // Si fallamos y no tenemos datos, mostramos algunos eventos locales
                if (recommendedEvents.value.isEmpty()) {
                    try {
                        val localEvents = eventRepository.getAllLocalEvents().take(5)
                        if (localEvents.isNotEmpty()) {
                            recommendedEvents.value = localEvents
                            recommendedCache.put(RECOMMENDED_KEY, localEvents)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeEventsViewModel", "Error loading recommended from local after Firebase failure: ${e.message}")
                    }
                }
            } finally {
                isLoadingRecommended.value = false
            }
        }
    }

    fun loadMoreRecommendedEvents() {
        if (isLoadingMoreRecommended.value || hasReachedEndRecommended.value) return

        // En modo offline, no cargamos más eventos recomendados
        if (_isOffline.value) {
            hasReachedEndRecommended.value = true
            return
        }

        isLoadingMoreRecommended.value = true
        viewModelScope.launch {
            try {
                Log.d("Current Recommended IDs", currentRecommendedIds.toString())
                val next = firebaseServicesFacade.fetchNextHomeRecommendedEvents(offsetIds = currentRecommendedIds)

                if (next.isNotEmpty()) {
                    // Filter out duplicates
                    val existingIds = recommendedEvents.value.map { it.name }.toSet()
                    val newEvents = next.filterNot { it.name in existingIds }

                    if (newEvents.isNotEmpty()) {
                        recommendedEvents.value += newEvents

                        // Update ids set
                        currentRecommendedIds.addAll(next.map { it.name })

                        // Add to GlobalData.allEvents
                        for (event in newEvents) {
                            if (!GlobalData.allEvents.contains(event)) {
                                GlobalData.allEvents.add(event)
                            }
                        }

                        // Update cache
                        recommendedCache.put(RECOMMENDED_KEY, recommendedEvents.value)
                    } else {
                        hasReachedEndRecommended.value = true
                    }
                } else {
                    hasReachedEndRecommended.value = true
                }
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error loading more recommended events: ${e.message}")
            } finally {
                isLoadingMoreRecommended.value = false
            }
        }
    }

    // Nearby
    private fun loadInitialNearbyEvents() {
        isLoadingNearby.value = true

        // Verificamos primero la caché
        val cached = nearbyCache.get(NEARBY_KEY)
        if (!cached.isNullOrEmpty()) {
            nearbyEvents.value = cached
            GlobalData.nearbyEvents = cached
            hasReachedEndNearby.value = false
        }

        // Si estamos offline, cargamos solo de Room
        if (_isOffline.value) {
            if (cached.isNullOrEmpty()) {
                viewModelScope.launch {
                    try {
                        val local = eventRepository.getAllLocalEvents()
                        val filtered = withContext(Dispatchers.IO) { filterWithinKm(local) }

                        if (filtered.isNotEmpty()) {
                            nearbyEvents.value = filtered
                            GlobalData.nearbyEvents = filtered
                            nearbyCache.put(NEARBY_KEY, filtered)

                            // Add to allEvents if not already there
                            for (event in filtered) {
                                if (!GlobalData.allEvents.contains(event)) {
                                    GlobalData.allEvents.add(event)
                                }
                            }
                        }
                        hasReachedEndNearby.value = filtered.isEmpty()
                    } catch (e: Exception) {
                        Log.e("HomeEventsViewModel", "Error loading nearby events from Room: ${e.message}")
                    } finally {
                        isLoadingNearby.value = false
                    }
                }
            } else {
                isLoadingNearby.value = false
            }
            return
        }

        // Modo online: cargamos de Firebase
        viewModelScope.launch {
            try {
                val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
                val filtered = withContext(Dispatchers.IO) { filterWithinKm(events) }

                // Update cache
                nearbyCache.put(NEARBY_KEY, filtered)

                // Merge with existing data
                val existingIds = nearbyEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                val newEvents = filtered.filterNot { "${it.name}-${it.startDate}" in existingIds }

                if (newEvents.isNotEmpty()) {
                    nearbyEvents.value += newEvents
                } else if (nearbyEvents.value.isEmpty()) {
                    nearbyEvents.value = filtered
                }

                // Update GlobalData
                GlobalData.nearbyEvents = nearbyEvents.value

                // Add to allEvents if not already there
                for (event in filtered) {
                    if (!GlobalData.allEvents.contains(event)) {
                        GlobalData.allEvents.add(event)
                    }
                }

                // Store in database
                eventRepository.storeEvents(filtered)

                lastHomeNearbySnapshot = snapshot
                hasReachedEndNearby.value = filtered.isEmpty()
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error fetching nearby events: ${e.message}")
                // Si fallamos y no tenemos datos, intentamos cargar de Room
                if (nearbyEvents.value.isEmpty()) {
                    try {
                        val local = eventRepository.getAllLocalEvents()
                        val filtered = withContext(Dispatchers.IO) { filterWithinKm(local) }

                        if (filtered.isNotEmpty()) {
                            nearbyEvents.value = filtered
                            GlobalData.nearbyEvents = filtered
                            nearbyCache.put(NEARBY_KEY, filtered)
                        }
                        hasReachedEndNearby.value = filtered.isEmpty()
                    } catch (e: Exception) {
                        Log.e("HomeEventsViewModel", "Error loading nearby from Room after Firebase failure: ${e.message}")
                    }
                }
            } finally {
                isLoadingNearby.value = false
            }
        }
    }

    fun loadMoreNearbyEvents() {
        if (isLoadingMoreNearby.value || hasReachedEndNearby.value) return

        // En modo offline, tratamos de cargar más de Room
        if (_isOffline.value) {
            isLoadingMoreNearby.value = true
            viewModelScope.launch {
                try {
                    val currentSize = nearbyEvents.value.size
                    val moreEvents = eventRepository.getEvents(5, currentSize)
                    val filtered = withContext(Dispatchers.IO) { filterWithinKm(moreEvents) }

                    if (filtered.isNotEmpty()) {
                        val existingIds = nearbyEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                        val newEvents = filtered.filterNot { "${it.name}-${it.startDate}" in existingIds }

                        if (newEvents.isNotEmpty()) {
                            nearbyEvents.value = nearbyEvents.value + newEvents
                            GlobalData.nearbyEvents = nearbyEvents.value
                            nearbyCache.put(NEARBY_KEY, nearbyEvents.value)
                        } else {
                            hasReachedEndNearby.value = true
                        }
                    } else {
                        hasReachedEndNearby.value = true
                    }
                } catch (e: Exception) {
                    Log.e("HomeEventsViewModel", "Error loading more nearby events offline: ${e.message}")
                } finally {
                    isLoadingMoreNearby.value = false
                }
            }
            return
        }

        isLoadingMoreNearby.value = true
        viewModelScope.launch {
            try {
                val (next, newSnap) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeNearbySnapshot)
                val filtered = withContext(Dispatchers.IO) { filterWithinKm(next) }

                if (filtered.isNotEmpty()) {
                    // Filter out duplicates
                    val existingIds = nearbyEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                    val newEvents = filtered.filterNot { "${it.name}-${it.startDate}" in existingIds }

                    if (newEvents.isNotEmpty()) {
                        nearbyEvents.value += newEvents
                        GlobalData.nearbyEvents = nearbyEvents.value

                        // Add to allEvents
                        for (event in newEvents) {
                            if (!GlobalData.allEvents.contains(event)) {
                                GlobalData.allEvents.add(event)
                            }
                        }

                        // Update cache
                        nearbyCache.put(NEARBY_KEY, nearbyEvents.value)

                        // Save to database
                        eventRepository.storeEvents(newEvents)

                        lastHomeNearbySnapshot = newSnap
                    } else {
                        hasReachedEndNearby.value = true
                    }
                } else {
                    hasReachedEndNearby.value = true
                }
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error loading more nearby events: ${e.message}")
            } finally {
                isLoadingMoreNearby.value = false
            }
        }
    }

    private fun List<Event>.filterUpcoming(): List<Event> {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return filter {
            val date = LocalDate.parse(it.startDate, fmt)
            !date.isBefore(today)
        }
    }

    private fun filterWithinKm(events: List<Event>): List<Event> {
        val (userLat, userLon) = LocationViewModel(getApplication()).getLastKnownLatLng()
        if (userLat == null || userLon == null) return emptyList()
        return events.filter {
            haversineKm(userLat, userLon, it.location.latitude, it.location.longitude) <= 5.0
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat/2).pow(2) + kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) * kotlin.math.sin(dLon/2).pow(2)
        val c = 2 * kotlin.math.atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
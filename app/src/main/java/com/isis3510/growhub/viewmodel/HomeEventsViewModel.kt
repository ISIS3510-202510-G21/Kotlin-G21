package com.isis3510.growhub.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.O)
class HomeEventsViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseServicesFacade = FirebaseServicesFacade()

    // States for event lists and loading states
    val upcomingEvents = mutableStateOf<List<Event>>(emptyList())
    val nearbyEvents = mutableStateOf<List<Event>>(emptyList())
    val recommendedEvents = mutableStateOf<List<Event>>(emptyList())

    val isLoadingUpcoming = mutableStateOf(false)
    val isLoadingNearby = mutableStateOf(false)
    val isLoadingRecommended = mutableStateOf(false)

    val connectivityViewModel = ConnectivityViewModel(application)
    private val _isOffline = mutableStateOf(false)
    val isOffline = _isOffline

    private val locationViewModel = LocationViewModel(application)

    private var lastHomeEventsSnapshot: DocumentSnapshot? = null
    private var currentRecommendedIds = mutableSetOf<String>()

    // States for paginated loading
    val isLoadingMoreUpcoming = mutableStateOf(false)
    val isLoadingMoreNearby = mutableStateOf(false)
    val isLoadingMoreRecommended = mutableStateOf(false)

    val hasReachedEndUpcoming = mutableStateOf(false)
    val hasReachedEndNearby = mutableStateOf(false)
    val hasReachedEndRecommended = mutableStateOf(false)

    private val db = AppLocalDatabase.getDatabase(application)
    private val eventRepository = EventRepository(db)

    // LRU Cache para upcoming, nearby y recommended events - almacena máximo 5 eventos cada uno
    private val eventsCache = LruCache<String, List<Event>>(2)
    private val timestampCache = LruCache<String, Long>(2)

    private val UPCOMING_CACHE_KEY = "upcoming_events"
    private val RECOMMENDED_CACHE_KEY = "recommended_events"
    private val UPCOMING_CACHE_TIMESTAMP_KEY = "upcoming_events_timestamp"
    private val RECOMMENDED_CACHE_TIMESTAMP_KEY = "recommended_events_timestamp"

    // Tiempo de expiración para el caché (24 horas en milisegundos)
    private val CACHE_EXPIRATION_TIME = 24 * 60 * 60 * 1000L

    init {
        // Observar cambios en el estado de conectividad
        viewModelScope.launch {
            connectivityViewModel.networkStatus.collectLatest { status ->
                _isOffline.value = status == ConnectionStatus.Unavailable || status == ConnectionStatus.Lost
                loadInitialHomeEvents()
                Log.d("HomeEventsViewModel", "Network status changed to: $status, isOffline: ${_isOffline.value}")
            }
        }

        loadInitialHomeEvents()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialHomeEvents() {
        Log.d("HomeEventsViewModel", "loadInitialHomeEvents: Start")
        loadInitialUpcomingEvents()
        loadInitialNearbyEvents()
        loadInitialRecommendedEvents()
        Log.d("HomeEventsViewModel", "loadInitialHomeEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialUpcomingEvents() {
        Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: Start")
        isLoadingUpcoming.value = true

        // Verificar si hay datos en el caché y si no han expirado
        val cachedEvents = eventsCache.get(UPCOMING_CACHE_KEY)
        val cachedTimestamp = timestampCache.get(UPCOMING_CACHE_TIMESTAMP_KEY) as? Long ?: 0L
        val currentTimeMillis = System.currentTimeMillis()

        if (cachedEvents != null &&
            (currentTimeMillis - cachedTimestamp < CACHE_EXPIRATION_TIME || isOffline.value)) {
            // Usar datos en caché si están frescos o si estamos offline
            Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: Using cached data")
            upcomingEvents.value = cachedEvents
            isLoadingUpcoming.value = false
            hasReachedEndUpcoming.value = cachedEvents.isEmpty()
            return
        }

        viewModelScope.launch {
            if (isOffline.value) {
                // Si estamos offline y no hay caché válido o ha expirado, cargar desde local
                Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: Device is offline, loading from local")
                loadInitialUpcomingEventsLocal()
                return@launch
            }

            Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: Calling firebaseServicesFacade.fetchHomeEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
            GlobalData.upcomingEvents = events
            if (events.isEmpty()) {
                Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: No events found, calling loadInitialUpcomingEventsLocal")
                isLoadingUpcoming.value = false
                hasReachedEndUpcoming.value = true
                loadInitialUpcomingEventsLocal()
                return@launch
            }
            else {
                val filteredEvents = events.filter { event ->
                    val startDate = event.startDate
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formattedDate = LocalDate.parse(startDate, formatter)
                    formattedDate.isAfter(today) || formattedDate == today
                }
                Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: Received ${filteredEvents.size} upcoming events from Facade")

                // Actualizar la interfaz
                upcomingEvents.value = filteredEvents

                // Guardar en Room para acceso offline posterior
                eventRepository.storeEvents(filteredEvents)

                // Guardar en el caché LRU los primeros 5 eventos
                val firstFiveEvents = filteredEvents.take(5)
                eventsCache.put(UPCOMING_CACHE_KEY, firstFiveEvents)

                lastHomeEventsSnapshot = snapshot
                isLoadingUpcoming.value = false
                hasReachedEndUpcoming.value = filteredEvents.isEmpty()
                Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: hasReachedEndUpcoming = $hasReachedEndUpcoming")
            }
        }
        Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: End")
    }

    private fun loadInitialUpcomingEventsLocal() {
        Log.d("HomeEventsViewModel", "loadInitialUpcomingEventsLocal: Start")
        isLoadingUpcoming.value = true
        viewModelScope.launch {
            // Verificar primero en el caché LRU
            val cachedEvents = eventsCache.get(UPCOMING_CACHE_KEY)
            if (cachedEvents != null) {
                Log.d("HomeEventsViewModel", "loadInitialUpcomingEventsLocal: Using LRU cached events")
                upcomingEvents.value = cachedEvents
                isLoadingUpcoming.value = false
                hasReachedEndUpcoming.value = cachedEvents.isEmpty()
                return@launch
            }

            // Si no hay en caché, cargar desde Room
            Log.d("HomeEventsViewModel", "loadInitialUpcomingEventsLocal: Calling eventRepository.getEvents")
            val events = eventRepository.getEvents(5, 0)
            val filteredEvents = events.filter { event ->
                val startDate = event.startDate
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formattedDate = LocalDate.parse(startDate, formatter)
                formattedDate.isAfter(today) || formattedDate == today
            }
            Log.d("HomeEventsViewModel", "loadInitialUpcomingEventsLocal: Received ${filteredEvents.size} upcoming events from local storage")

            // Actualizar la interfaz
            upcomingEvents.value = filteredEvents

            // Actualizar también el caché LRU
            eventsCache.put(UPCOMING_CACHE_KEY, filteredEvents)

            isLoadingUpcoming.value = false
            hasReachedEndUpcoming.value = filteredEvents.isEmpty()
            Log.d("HomeEventsViewModel", "loadInitialUpcomingEventsLocal: hasReachedEndUpcoming = $hasReachedEndUpcoming")
        }
    }

    // Méthod para limpiar el caché
    fun clearEventsCache() {
        eventsCache.remove(UPCOMING_CACHE_KEY)
        eventsCache.remove(RECOMMENDED_CACHE_KEY)
        timestampCache.remove(UPCOMING_CACHE_TIMESTAMP_KEY)
        timestampCache.remove(RECOMMENDED_CACHE_TIMESTAMP_KEY)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreUpcomingEvents() {
        if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
            return
        }

        Log.d("HomeEventsViewModel", "loadMoreUpcomingEvents: Start - isLoadingMoreUpcoming = ${isLoadingMoreUpcoming.value}, hasReachedEndUpcoming = ${hasReachedEndUpcoming.value}")
        if (isLoadingMoreUpcoming.value || hasReachedEndUpcoming.value) return

        isLoadingMoreUpcoming.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadMoreUpcomingEvents: Calling firebaseServicesFacade.fetchNextHomeEvents")
            val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeEventsSnapshot)
            val filteredNextEvents = nextEvents
                .filter { event ->
                    val startDate = event.startDate
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formattedDate = LocalDate.parse(startDate, formatter)
                    formattedDate.isAfter(today) || formattedDate == today
                }
            Log.d("HomeEventsViewModel", "loadMoreUpcomingEvents: Received ${filteredNextEvents.size} next upcoming events from Facade")
            if (filteredNextEvents.isNotEmpty()) {
                upcomingEvents.value += filteredNextEvents
                lastHomeEventsSnapshot = newLastSnapshot
            } else {
                hasReachedEndUpcoming.value = true
                Log.d("HomeEventsViewModel", "loadMoreUpcomingEvents: No more upcoming events, hasReachedEndUpcoming set to true")
            }
            isLoadingMoreUpcoming.value = false
            Log.d("HomeEventsViewModel", "loadMoreUpcomingEvents: isLoadingMoreUpcoming set to false")
        }
        Log.d("HomeEventsViewModel", "loadMoreUpcomingEvents: End")
    }


    private fun filterWithinKm(events: List<Event>): List<Event> {
        // Recupera coords de usuario
        val (userLat, userLon) = locationViewModel.getLastKnownLatLng()
        if (userLat == null || userLon == null) return emptyList()

        return events.filter { event ->
            // Asume que event.latitude y event.longitude son Doubles
            val dist = haversineDistanceKm(
                userLat, userLon,
                event.location.latitude, event.location.longitude
            )
            dist <= 5.0
        }
    }

    private fun haversineDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialNearbyEvents() {
        Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: Start")
        isLoadingNearby.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: Calling firebaseServicesFacade.fetchHomeEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()

            val filtered = withContext(Dispatchers.IO) {
                filterWithinKm(events)
            }

            GlobalData.nearbyEvents = filtered
            eventRepository.storeEvents(filtered)
            if (filtered.isEmpty() || connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
                Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: No events found, calling loadInitialNearbyEventsLocal")
                isLoadingNearby.value = false
                hasReachedEndNearby.value = true
                loadInitialNearbyEventsLocal()
                return@launch
            }
            else {
                Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: Received ${events.size} nearby events from Facade")
                nearbyEvents.value = events
                lastHomeEventsSnapshot = snapshot
                isLoadingNearby.value = false
                hasReachedEndNearby.value = events.isEmpty()
                Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: hasReachedEndNearby = $hasReachedEndNearby")
            }
        }
        Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: End")
    }

    private fun loadInitialNearbyEventsLocal() {
        Log.d("HomeEventsViewModel", "loadInitialNearbyEventsLocal: Start")
        isLoadingNearby.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadInitialNearbyEventsLocal: Calling eventRepository.getEvents")
            val events = eventRepository.getFreeEvents()
            GlobalData.nearbyEvents = events
            Log.d("HomeEventsViewModel", "loadInitialNearbyEventsLocal: Received ${events.size} nearby events from local storage")
            nearbyEvents.value = events
            isLoadingNearby.value = false
            hasReachedEndNearby.value = events.isEmpty()
            Log.d("HomeEventsViewModel", "loadInitialNearbyEventsLocal: hasReachedEndNearby = $hasReachedEndNearby")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreNearbyEvents() {
        if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
            return
        }

        Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: Start - isLoadingMoreNearby = ${isLoadingMoreNearby.value}, hasReachedEndNearby = ${hasReachedEndNearby.value}")
        if (isLoadingMoreNearby.value || hasReachedEndNearby.value) return

        isLoadingMoreNearby.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: Calling firebaseServicesFacade.fetchNextHomeEvents")
            val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeEventsSnapshot)
            Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: Received ${nextEvents.size} next nearby events from Facade")
            if (nextEvents.isNotEmpty()) {
                nearbyEvents.value += nextEvents
                GlobalData.nearbyEvents = nearbyEvents.value
                lastHomeEventsSnapshot = newLastSnapshot
            } else {
                hasReachedEndNearby.value = true

                Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: No more nearby events, hasReachedEndNearby set to true")
            }
            isLoadingMoreNearby.value = false
            Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: isLoadingMoreNearby set to false")
        }
        Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: End")
    }


    // --- Recommended Events con caché LRU ---
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialRecommendedEvents() {
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Start")
        isLoadingRecommended.value = true

        val now = System.currentTimeMillis()

        viewModelScope.launch {
            if (isOffline.value) {
                Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Offline, loading local")
                loadInitialRecommendedEventsLocal()
                return@launch
            }

            Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Calling firebaseServicesFacade.fetchHomeRecommendedEvents")
            val (events) = firebaseServicesFacade.fetchHomeRecommendedEvents()
            recommendedEvents.value = events
            currentRecommendedIds.clear()
            currentRecommendedIds.addAll(events.map { it.name })

            isLoadingRecommended.value = false
            hasReachedEndRecommended.value = events.isEmpty()

            // Cache first 5
            eventsCache.put(RECOMMENDED_CACHE_KEY, events.take(5))
            timestampCache.put(RECOMMENDED_CACHE_KEY, now)
            Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Cached ${events.take(5).size} recommended events")
        }
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: End")
    }

    private fun loadInitialRecommendedEventsLocal() {
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: Start")
        isLoadingRecommended.value = true
        val ts = timestampCache.get(RECOMMENDED_CACHE_KEY) ?: 0L
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            val cached = eventsCache.get(RECOMMENDED_CACHE_KEY)
            if (cached != null && (now - ts < CACHE_EXPIRATION_TIME || isOffline.value)) {
                Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: Using cached data")
                recommendedEvents.value = cached
                currentRecommendedIds.clear()
                currentRecommendedIds.addAll(cached.map { it.name })
                isLoadingRecommended.value = false
                hasReachedEndRecommended.value = cached.isEmpty()
                return@launch
            }
            val events = eventRepository.getEvents(5, 0)
            recommendedEvents.value = events
            isLoadingRecommended.value = false
            hasReachedEndRecommended.value = events.isEmpty()

            // Update cache
            eventsCache.put(RECOMMENDED_CACHE_KEY, events)
            timestampCache.put(RECOMMENDED_CACHE_KEY, System.currentTimeMillis())
            Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: Cached ${events.size} recommended events locally")
        }
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: End")
    }

    fun loadMoreRecommendedEvents() {
        Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Start - isLoadingMoreRecommended = ${isLoadingMoreRecommended.value}, hasReachedEndRecommended = ${hasReachedEndRecommended.value}")
        if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
            return
        }
        if (isLoadingMoreRecommended.value || hasReachedEndRecommended.value) return
        isLoadingMoreRecommended.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Calling firebaseServicesFacade.fetchNextHomeRecommendedEvents, currentRecommendedIds size = ${currentRecommendedIds.size}")
            val nextEvents = firebaseServicesFacade.fetchNextHomeRecommendedEvents(offsetIds = currentRecommendedIds)
            Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Received ${nextEvents.size} next recommended events from Facade")
            if (nextEvents.isNotEmpty()) {
                recommendedEvents.value += nextEvents
                val newIds = nextEvents.map { it.name }
                currentRecommendedIds.addAll(newIds)
                Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Updated currentRecommendedIds to ${currentRecommendedIds.size} elements")
            } else {
                hasReachedEndRecommended.value = true
                Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: No more recommended events, hasReachedEndRecommended set to true")
            }
            isLoadingMoreRecommended.value = false
            Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: isLoadingMoreRecommended set to false")
        }
        Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: End")
    }
}

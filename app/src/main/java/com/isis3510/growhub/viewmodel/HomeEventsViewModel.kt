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

        // First load from cache/database to show something immediately
        loadUpcomingFromCacheOrRoom()

        // If offline, we already loaded from cache or room, so we're done
        if (_isOffline.value) return

        // Otherwise, fetch fresh data from Firebase
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
                // If we failed to fetch, at least we already loaded from cache
            } finally {
                isLoadingUpcoming.value = false
            }
        }
    }

    private fun loadUpcomingFromCacheOrRoom() {
        val cached = upcomingCache[UPCOMING_KEY]
        if (!cached.isNullOrEmpty()) {
            upcomingEvents.value = cached
            GlobalData.upcomingEvents = cached
            hasReachedEndUpcoming.value = false
            return
        }

        viewModelScope.launch {
            try {
                val room = eventRepository.getEvents(10, 0).filterUpcoming()
                if (room.isNotEmpty()) {
                    upcomingEvents.value = room
                    GlobalData.upcomingEvents = room
                    upcomingCache.put(UPCOMING_KEY, room)
                }
                hasReachedEndUpcoming.value = room.isEmpty()
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error loading upcoming events from Room: ${e.message}")
            }
        }
    }

    fun loadMoreUpcomingEvents() {
        if (_isOffline.value) return
        if (isLoadingMoreUpcoming.value || hasReachedEndUpcoming.value) return

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

        // First load from cache/database
        loadRecommendedFromCacheOrRoom()

        // If offline, we're done
        if (_isOffline.value) {
            Log.d("HomeEventsViewModel", "Offline mode, not fetching recommended events")
            return
        }

        viewModelScope.launch {
            try {
                val (events) = firebaseServicesFacade.fetchHomeRecommendedEvents()

                // Update ids set
                currentRecommendedIds = events.mapTo(mutableSetOf()) { it.name + "-" + it.startDate }

                // Update cache
                recommendedCache.put(RECOMMENDED_KEY, events)

                // Merge with existing data
                val existingIds = recommendedEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                val newEvents = events.filterNot { "${it.name}-${it.startDate}" in existingIds }

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
            } finally {
                isLoadingRecommended.value = false
            }
        }
    }

    private fun loadRecommendedFromCacheOrRoom() {
        val cached = recommendedCache[RECOMMENDED_KEY]
        if (!cached.isNullOrEmpty()) {
            recommendedEvents.value = cached
            hasReachedEndRecommended.value = false
            return
        }

        viewModelScope.launch {
            try {
                val room = eventRepository.getEvents(10, 0)
                if (room.isNotEmpty()) {
                    recommendedEvents.value = room
                    recommendedCache.put(RECOMMENDED_KEY, room)
                }
                hasReachedEndRecommended.value = room.isEmpty()
            } catch (e: Exception) {
                Log.e("HomeEventsViewModel", "Error loading recommended events from Room: ${e.message}")
            }
        }
    }

    fun loadMoreRecommendedEvents() {
        if (_isOffline.value) return
        if (isLoadingMoreRecommended.value || hasReachedEndRecommended.value) return

        isLoadingMoreRecommended.value = true
        viewModelScope.launch {
            try {
                val next = firebaseServicesFacade.fetchNextHomeRecommendedEvents(offsetIds = currentRecommendedIds)

                if (next.isNotEmpty()) {
                    // Filter out duplicates
                    val existingIds = recommendedEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                    val newEvents = next.filterNot { "${it.name}-${it.startDate}" in existingIds }

                    if (newEvents.isNotEmpty()) {
                        recommendedEvents.value += newEvents

                        // Update ids set
                        currentRecommendedIds.addAll(next.map { it.name + "-" + it.startDate })

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

        // First load from cache or database
        loadNearbyFromCacheOrRoom()

        // If offline, we're done
        if (_isOffline.value) return

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
            } finally {
                isLoadingNearby.value = false
            }
        }
    }

    private fun loadNearbyFromCacheOrRoom() {
        val cached = nearbyCache[NEARBY_KEY]
        if (!cached.isNullOrEmpty()) {
            nearbyEvents.value = cached
            GlobalData.nearbyEvents = cached
            hasReachedEndNearby.value = false
            return
        }

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
            }
        }
    }

    fun loadMoreNearbyEvents() {
        if (_isOffline.value) return
        if (isLoadingMoreNearby.value || hasReachedEndNearby.value) return

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
            haversineKm(userLat, userLon, it.location.latitude, it.location.longitude) <= 7.0
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
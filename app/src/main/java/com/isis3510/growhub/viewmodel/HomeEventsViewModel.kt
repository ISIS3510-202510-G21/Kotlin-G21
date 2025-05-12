package com.isis3510.growhub.viewmodel

import android.app.Application
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
import kotlin.math.pow
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.O)
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

    private val upcomingCache = LruCache<String, List<Event>>(1)
    private val recommendedCache = LruCache<String, List<Event>>(1)

    private val UPCOMING_KEY = "upcoming_events"
    private val RECOMMENDED_KEY = "recommended_events"

    init {
        viewModelScope.launch {
            connectivityViewModel.networkStatus.collectLatest { status ->
                _isOffline.value = status == ConnectionStatus.Unavailable || status == ConnectionStatus.Lost
                loadInitialHomeEvents()
                Log.d("HomeEventsViewModel", "Network status: $status, isOffline=${_isOffline.value}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialHomeEvents() {
        loadInitialUpcomingEvents()
        loadInitialRecommendedEvents()
        loadInitialNearbyEvents()
    }

    // Upcoming
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialUpcomingEvents() {
        isLoadingUpcoming.value = true
        if (_isOffline.value) return loadUpcomingFromCacheOrRoom()
        viewModelScope.launch {
            val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
            val filtered = events.filterUpcoming()
            upcomingEvents.value = filtered
            eventRepository.storeEvents(filtered)
            upcomingCache.put(UPCOMING_KEY, filtered.take(5))
            lastHomeUpcomingSnapshot = snapshot
            isLoadingUpcoming.value = false
            hasReachedEndUpcoming.value = filtered.isEmpty()
        }
    }

    private fun loadUpcomingFromCacheOrRoom() {
        val cached = upcomingCache.get(UPCOMING_KEY)
        if (!cached.isNullOrEmpty()) {
            upcomingEvents.value = cached
            isLoadingUpcoming.value = false
            hasReachedEndUpcoming.value = cached.isEmpty()
            return
        }
        viewModelScope.launch {
            val room = eventRepository.getEvents(5, 0).filterUpcoming()
            upcomingEvents.value = room
            isLoadingUpcoming.value = false
            hasReachedEndUpcoming.value = room.isEmpty()
            upcomingCache.put(UPCOMING_KEY, room)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreUpcomingEvents() {
        if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) return
        if (isLoadingMoreUpcoming.value || hasReachedEndUpcoming.value) return
        isLoadingMoreUpcoming.value = true
        viewModelScope.launch {
            val (next, newSnap) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeUpcomingSnapshot)
            val filteredNext = next.filterUpcoming()
            if (filteredNext.isNotEmpty()) {
                upcomingEvents.value += filteredNext
                lastHomeUpcomingSnapshot = newSnap
            } else hasReachedEndUpcoming.value = true
            isLoadingMoreUpcoming.value = false
        }
    }

    // Recommended
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialRecommendedEvents() {
        isLoadingRecommended.value = true
        if (_isOffline.value) return loadRecommendedFromCacheOrRoom()
        viewModelScope.launch {
            val (events) = firebaseServicesFacade.fetchHomeRecommendedEvents()
            recommendedEvents.value = events
            currentRecommendedIds = events.mapTo(mutableSetOf()) { it.name + "-" + it.startDate }
            recommendedCache.put(RECOMMENDED_KEY, events.take(5))
            isLoadingRecommended.value = false
            hasReachedEndRecommended.value = events.isEmpty()
        }
    }

    private fun loadRecommendedFromCacheOrRoom() {
        val cached = recommendedCache.get(RECOMMENDED_KEY)
        if (!cached.isNullOrEmpty()) {
            recommendedEvents.value = cached
            isLoadingRecommended.value = false
            hasReachedEndRecommended.value = cached.isEmpty()
            return
        }
        viewModelScope.launch {
            val room = eventRepository.getEvents(5, 0)
            recommendedEvents.value = room
            isLoadingRecommended.value = false
            hasReachedEndRecommended.value = room.isEmpty()
            recommendedCache.put(RECOMMENDED_KEY, room)
        }
    }

    fun loadMoreRecommendedEvents() {
        if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) return
        if (isLoadingMoreRecommended.value || hasReachedEndRecommended.value) return
        isLoadingMoreRecommended.value = true
        viewModelScope.launch {
            val next = firebaseServicesFacade.fetchNextHomeRecommendedEvents(offsetIds = currentRecommendedIds)
            if (next.isNotEmpty()) {
                recommendedEvents.value += next
                currentRecommendedIds.addAll(next.map { it.name + "-" + it.startDate })
            } else hasReachedEndRecommended.value = true
            isLoadingMoreRecommended.value = false
        }
    }

    // Nearby
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialNearbyEvents() {
        isLoadingNearby.value = true
        if (_isOffline.value) {
            viewModelScope.launch {
                val local = eventRepository.getAllLocalEvents()
                val filtered = withContext(Dispatchers.IO) { filterWithinKm(local) }
                GlobalData.nearbyEvents = filtered

                nearbyEvents.value = filtered
                isLoadingNearby.value = false
                hasReachedEndNearby.value = filtered.isEmpty()
            }
            return
        }
        viewModelScope.launch {
            val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
            val filtered = withContext(Dispatchers.IO) { filterWithinKm(events) }
            nearbyEvents.value = filtered
            GlobalData.nearbyEvents = filtered
            eventRepository.storeEvents(filtered)
            lastHomeNearbySnapshot = snapshot
            isLoadingNearby.value = false
            hasReachedEndNearby.value = filtered.isEmpty()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreNearbyEvents() {
        if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) return
        if (isLoadingMoreNearby.value || hasReachedEndNearby.value) return
        isLoadingMoreNearby.value = true
        viewModelScope.launch {
            val (next, newSnap) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeNearbySnapshot)
            val filtered = withContext(Dispatchers.IO) { filterWithinKm(next) }
            if (filtered.isNotEmpty()) {
                nearbyEvents.value += filtered
                GlobalData.nearbyEvents = nearbyEvents.value
                lastHomeNearbySnapshot = newSnap
            } else hasReachedEndNearby.value = true
            isLoadingMoreNearby.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat/2).pow(2) + kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) * kotlin.math.sin(dLon/2).pow(2)
        val c = 2 * kotlin.math.atan2(sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }
}

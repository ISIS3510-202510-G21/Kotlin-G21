package com.isis3510.growhub.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.data.GlobalData
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    init {
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
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: Calling firebaseServicesFacade.fetchHomeEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
            if (events.isEmpty()) {
                //Log.d("HomeEventsViewModel", "loadInitialUpcomingEvents: No events found, calling loadInitialUpcomingEventsLocal")
                //isLoadingUpcoming.value = false
                //hasReachedEndUpcoming.value = true
                //loadInitialUpcomingEventsLocal()
                //return@launch
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
                upcomingEvents.value = filteredEvents
                //eventRepository.storeEvents(filteredEvents)
                // Check if events are being stored properly
                //val storedEvents = eventRepository.getEvents(5, 0)
                //Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: Stored ${storedEvents.size} upcoming events")
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
            upcomingEvents.value = filteredEvents
            isLoadingUpcoming.value = false
            hasReachedEndUpcoming.value = filteredEvents.isEmpty()
            Log.d("HomeEventsViewModel", "loadInitialUpcomingEventsLocal: hasReachedEndUpcoming = $hasReachedEndUpcoming")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreUpcomingEvents() {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialNearbyEvents() {
        Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: Start")
        isLoadingNearby.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: Calling firebaseServicesFacade.fetchHomeEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchHomeEvents()
            GlobalData.nearbyEvents = events
            if (events.isEmpty()) {
                //Log.d("HomeEventsViewModel", "loadInitialNearbyEvents: No events found, calling loadInitialNearbyEventsLocal")
                //isLoadingNearby.value = false
                //hasReachedEndNearby.value = true
                //loadInitialNearbyEventsLocal()
                //return@launch
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
            val events = eventRepository.getEvents(5, 0)
            Log.d("HomeEventsViewModel", "loadInitialNearbyEventsLocal: Received ${events.size} nearby events from local storage")
            nearbyEvents.value = events
            isLoadingNearby.value = false
            hasReachedEndNearby.value = events.isEmpty()
            Log.d("HomeEventsViewModel", "loadInitialNearbyEventsLocal: hasReachedEndNearby = $hasReachedEndNearby")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreNearbyEvents() {
        Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: Start - isLoadingMoreNearby = ${isLoadingMoreNearby.value}, hasReachedEndNearby = ${hasReachedEndNearby.value}")
        if (isLoadingMoreNearby.value || hasReachedEndNearby.value) return

        isLoadingMoreNearby.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: Calling firebaseServicesFacade.fetchNextHomeEvents")
            val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextHomeEvents(lastSnapshot = lastHomeEventsSnapshot)
            Log.d("HomeEventsViewModel", "loadMoreNearbyEvents: Received ${nextEvents.size} next nearby events from Facade")
            if (nextEvents.isNotEmpty()) {
                nearbyEvents.value += nextEvents
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialRecommendedEvents() {
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Start")
        isLoadingRecommended.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Calling firebaseServicesFacade.fetchHomeRecommendedEvents")
            val (events) = firebaseServicesFacade.fetchHomeRecommendedEvents()
            if (events.isEmpty()) {
                //Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: No events found, calling loadInitialRecommendedEventsLocal")
                //isLoadingRecommended.value = false
                //hasReachedEndRecommended.value = true
                //loadInitialRecommendedEventsLocal()
                //return@launch
            } else {
                Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Received ${events.size} recommended events from Facade")
                recommendedEvents.value = events
                //eventRepository.storeEvents(events)
                // Check if events are being stored properly
                //val storedEvents = eventRepository.getEvents(5, 0)
                //Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: Stored ${storedEvents.size} recommended events")
                val newIds = events.map { it.id }
                currentRecommendedIds.addAll(newIds)
                isLoadingRecommended.value = false
                hasReachedEndRecommended.value = events.isEmpty()
                Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: hasReachedEndRecommended = $hasReachedEndRecommended")
            }
        }
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEvents: End")
    }

    private fun loadInitialRecommendedEventsLocal() {
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: Start")
        isLoadingRecommended.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: Calling eventRepository.getEvents")
            val events = eventRepository.getEvents(5, 0)
            Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: Received ${events.size} recommended events from local storage")
            recommendedEvents.value = events
            isLoadingRecommended.value = false
            hasReachedEndRecommended.value = events.isEmpty()
            Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: hasReachedEndRecommended = $hasReachedEndRecommended")
        }
        Log.d("HomeEventsViewModel", "loadInitialRecommendedEventsLocal: End")
    }

    fun loadMoreRecommendedEvents() {
        Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Start - isLoadingMoreRecommended = ${isLoadingMoreRecommended.value}, hasReachedEndRecommended = ${hasReachedEndRecommended.value}")
        if (isLoadingMoreRecommended.value || hasReachedEndRecommended.value) return
        isLoadingMoreRecommended.value = true
        viewModelScope.launch {
            Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Calling firebaseServicesFacade.fetchNextHomeRecommendedEvents, currentRecommendedIds size = ${currentRecommendedIds.size}")
            val nextEvents = firebaseServicesFacade.fetchNextHomeRecommendedEvents(offsetIds = currentRecommendedIds)
            Log.d("HomeEventsViewModel", "loadMoreRecommendedEvents: Received ${nextEvents.size} next recommended events from Facade")
            if (nextEvents.isNotEmpty()) {
                recommendedEvents.value += nextEvents
                val newIds = nextEvents.map { it.id }
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
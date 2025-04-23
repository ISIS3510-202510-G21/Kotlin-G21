package com.isis3510.growhub.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.utils.ConnectionStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
class MyEventsViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseServicesFacade = FirebaseServicesFacade()
    val isLoadingUpcoming = mutableStateOf(false)
    val isLoadingPrevious = mutableStateOf(false)
    val isLoadingCreatedByMe = mutableStateOf(false)

    val upcomingEvents = mutableStateOf<List<Event>>(emptyList())
    val previousEvents = mutableStateOf<List<Event>>(emptyList())
    val createdByMeEvents = mutableStateOf<List<Event>>(emptyList())

    private var lastMyEventsSnapshot: DocumentSnapshot? = null

    val isLoadingMoreUpcoming = mutableStateOf(false)
    val isLoadingMorePrevious = mutableStateOf(false)
    val isLoadingMoreCreatedByMe = mutableStateOf(false)
    val hasReachedEnd = mutableStateOf(false)

    private val db = AppLocalDatabase.getDatabase(application)
    private val eventRepository = EventRepository(db)
    private val connectivityViewModel = ConnectivityViewModel(application)

    init {
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            connectivityViewModel.networkStatus.collectLatest { status ->
                loadInitialMyEvents(isNetworkAvailable = status)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialMyEvents(isNetworkAvailable : ConnectionStatus) {
        Log.d("MyEventsViewModel", "loadInitialMyEvents: Start")
        Log.d("MyEventsViewModel", "Network status: $isNetworkAvailable")
        if (isNetworkAvailable == ConnectionStatus.Available) {
            loadInitialUpcomingEvents()
            loadInitialPreviousEvents()
            loadInitialCreatedByMeEvents()
        } else {
            loadInitialUpcomingEventsLocal()
            loadInitialPreviousEventsLocal()
            loadInitialCreatedByMeEventsLocal()
        }
        Log.d("MyEventsViewModel", "loadInitialMyEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialUpcomingEvents() {
        Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: Start")
        isLoadingUpcoming.value = true
        viewModelScope.launch {
            Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: Calling firebaseServicesFacade.fetchMyEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchMyEvents()
            val filteredEvents = events
                .filter { event ->
                    val startDate = event.startDate
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formattedDate = LocalDate.parse(startDate, formatter)
                    formattedDate.isAfter(today) || formattedDate == today
                }
            Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: Received ${filteredEvents.size} upcoming events from Facade")
            upcomingEvents.value = filteredEvents
            eventRepository.storeEvents(filteredEvents)
            // Check if events are being stored properly
            val storedEvents = eventRepository.getEvents(5, 0)
            Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: Stored ${storedEvents.size} upcoming events")
            lastMyEventsSnapshot = snapshot
            isLoadingUpcoming.value = false
            hasReachedEnd.value = filteredEvents.isEmpty()
            Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: hasReachedEnd = $hasReachedEnd")
        }
        Log.d("MyEventsViewModel", "loadInitialUpcomingEvents: End")
    }

    private fun loadInitialUpcomingEventsLocal() {
        Log.d("MyEventsViewModel", "loadInitialUpcomingEventsLocal: Start")
        isLoadingUpcoming.value = true
        viewModelScope.launch {
            Log.d("MyEventsViewModel", "loadInitialUpcomingEventsLocal: Calling eventRepository.getEvents")
            val events = eventRepository.getEvents(5, 0)
            val filteredEvents = events.filter { event ->
                val startDate = event.startDate
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formattedDate = LocalDate.parse(startDate, formatter)
                formattedDate.isAfter(today) || formattedDate == today
            }
            Log.d("MyEventsViewModel", "loadInitialUpcomingEventsLocal: Received ${filteredEvents.size} upcoming events from local storage")
            upcomingEvents.value = filteredEvents
            isLoadingUpcoming.value = false
            hasReachedEnd.value = filteredEvents.isEmpty()
            Log.d("MyEventsViewModel", "loadInitialUpcomingEventsLocal: hasReachedEnd = $hasReachedEnd")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreUpcomingEvents() {
        Log.d("MyEventsViewModel", "loadMoreUpcomingEvents: Start - isLoadingMore = ${isLoadingMoreUpcoming.value}, hasReachedEnd = ${hasReachedEnd.value}")
        if (isLoadingMoreUpcoming.value || hasReachedEnd.value) return

        isLoadingMoreUpcoming.value = true
        viewModelScope.launch {
            try {
                Log.d("MyEventsViewModel", "Calling firebaseServicesFacade.fetchNextMyEvents with lastSnapshot = $lastMyEventsSnapshot")
                val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextMyEvents(lastSnapshot = lastMyEventsSnapshot)
                val filteredNextEvents = nextEvents
                    .filter { event ->
                        val startDate = event.startDate
                        val today = LocalDate.now()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val formattedDate = LocalDate.parse(startDate, formatter)
                        formattedDate.isAfter(today) || formattedDate == today
                    }
                Log.d("MyEventsViewModel", "Received ${filteredNextEvents.size} new events")

                if (filteredNextEvents.isNotEmpty()) {
                    upcomingEvents.value += filteredNextEvents
                    lastMyEventsSnapshot = newLastSnapshot
                } else {
                    hasReachedEnd.value = true
                    Log.d("MyEventsViewModel", "No more upcoming events, hasReachedEnd set to true")
                }

            } catch (e: Exception) {
                Log.e("MyEventsViewModel", "Error loading more upcoming events", e)
            } finally {
                isLoadingMoreUpcoming.value = false
                Log.d("MyEventsViewModel", "loadMoreUpcomingEvents: isLoadingMore set to false")
            }
        }
        Log.d("MyEventsViewModel", "loadMoreMyEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialPreviousEvents() {
        Log.d("MyEventsViewModel", "loadInitialPreviousEvents: Start")
        isLoadingPrevious.value = true
        viewModelScope.launch {
            Log.d("MyEventsViewModel", "loadInitialPreviousEvents: Calling firebaseServicesFacade.fetchMyEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchMyEvents()
            val filteredEvents = events
                .filter { event ->
                    val startDate = event.startDate
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formattedDate = LocalDate.parse(startDate, formatter)
                    formattedDate.isBefore(today)
                }
            Log.d("MyEventsViewModel", "loadInitialPreviousEvents: Received ${filteredEvents.size} previous events from Facade")
            previousEvents.value = filteredEvents
            eventRepository.storeEvents(filteredEvents)
            // Check if events are being stored properly
            val storedEvents = eventRepository.getEvents(5, 0)
            Log.d("MyEventsViewModel", "loadInitialPreviousEvents: Stored ${storedEvents.size} previous events")
            lastMyEventsSnapshot = snapshot
            isLoadingPrevious.value = false
            hasReachedEnd.value = filteredEvents.isEmpty()
            Log.d("MyEventsViewModel", "loadInitialPreviousEvents: hasReachedEnd = $hasReachedEnd")
        }
        Log.d("MyEventsViewModel", "loadInitialPreviousEvents: End")
    }

    private fun loadInitialPreviousEventsLocal() {
        Log.d("MyEventsViewModel", "loadInitialPreviousEventsLocal: Start")
        isLoadingPrevious.value = true
        viewModelScope.launch {
            Log.d("MyEventsViewModel", "loadInitialPreviousEventsLocal: Calling eventRepository.getEvents")
            val events = eventRepository.getEvents(5, 0)
            val filteredEvents = events.filter { event ->
                val startDate = event.startDate
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formattedDate = LocalDate.parse(startDate, formatter)
                formattedDate.isBefore(today) || formattedDate == today
            }
            Log.d("MyEventsViewModel", "loadInitialPreviousEventsLocal: Received ${filteredEvents.size} previous events from local storage")
            previousEvents.value = filteredEvents
            // Log events
            Log.d("MyEventsViewModel", "loadInitialPreviousEventsLocal: Events: $filteredEvents")
            isLoadingPrevious.value = false
            hasReachedEnd.value = filteredEvents.isEmpty()
            Log.d("MyEventsViewModel", "loadInitialPreviousEventsLocal: hasReachedEnd = $hasReachedEnd")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMorePreviousEvents() {
        Log.d("MyEventsViewModel", "loadMorePreviousEvents: Start - isLoadingMore = ${isLoadingMorePrevious.value}, hasReachedEnd = ${hasReachedEnd.value}")
        if (isLoadingMorePrevious.value || hasReachedEnd.value) return

        isLoadingMorePrevious.value = true
        viewModelScope.launch {
            try {
                Log.d("MyEventsViewModel", "Calling firebaseServicesFacade.fetchNextMyEvents with lastSnapshot = $lastMyEventsSnapshot")
                val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextMyEvents(lastSnapshot = lastMyEventsSnapshot)
                val filteredNextEvents = nextEvents
                    .filter { event ->
                        val startDate = event.startDate
                        val today = LocalDate.now()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val formattedDate = LocalDate.parse(startDate, formatter)
                        formattedDate.isBefore(today)
                    }
                Log.d("MyEventsViewModel", "Received ${nextEvents.size} new events")

                if (filteredNextEvents.isNotEmpty()) {
                    previousEvents.value += filteredNextEvents
                    lastMyEventsSnapshot = newLastSnapshot
                } else {
                    hasReachedEnd.value = true
                    Log.d("MyEventsViewModel", "No more previous events, hasReachedEnd set to true")
                }

            } catch (e: Exception) {
                Log.e("MyEventsViewModel", "Error loading more previous events", e)
            } finally {
                isLoadingMorePrevious.value = false
                Log.d("MyEventsViewModel", "loadMorePreviousEvents: isLoadingMore set to false")
            }
        }
        Log.d("MyEventsViewModel", "loadMorePreviousEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialCreatedByMeEvents() {
        Log.d("MyEventsViewModel", "loadInitialCreatedByMeEvents: Start")
        isLoadingCreatedByMe.value = true
        viewModelScope.launch {
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEvents: Calling firebaseServicesFacade.fetchMyEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchMyEventsCreate()
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEvents: Received ${events.size} created by me events from Facade")
            createdByMeEvents.value = events
            // Check if events are being stored properly
            val storedEvents = eventRepository.getEvents(5, 0)
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEvents: Stored ${storedEvents.size} created by me events")
            lastMyEventsSnapshot = snapshot
            isLoadingCreatedByMe.value = false
            hasReachedEnd.value = events.isEmpty()
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEvents: hasReachedEnd = $hasReachedEnd")
        }
        Log.d("MyEventsViewModel", "loadInitialCreatedByMeEvents: End")
    }

    private fun loadInitialCreatedByMeEventsLocal() {
        Log.d("MyEventsViewModel", "loadInitialCreatedByMeEventsLocal: Start")
        isLoadingCreatedByMe.value = true
        viewModelScope.launch {
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEventsLocal: Calling eventRepository.getEventsCreate")
            val events = eventRepository.getEvents(5, 0)
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEventsLocal: Received ${events.size} created by me events from local storage")
            createdByMeEvents.value = events
            eventRepository.storeEvents(events)
            isLoadingCreatedByMe.value = false
            hasReachedEnd.value = events.isEmpty()
            Log.d("MyEventsViewModel", "loadInitialCreatedByMeEventsLocal: hasReachedEnd = $hasReachedEnd")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreCreatedByMeEvents() {
        Log.d("MyEventsViewModel", "loadMoreCreatedByMeEvents: Start - isLoadingMore = ${isLoadingMoreCreatedByMe.value}, hasReachedEnd = ${hasReachedEnd.value}")
        if (isLoadingMoreCreatedByMe.value || hasReachedEnd.value) return

        isLoadingMoreCreatedByMe.value = true
        viewModelScope.launch {
            try {
                Log.d("MyEventsViewModel", "Calling firebaseServicesFacade.fetchNextMyEvents with lastSnapshot = $lastMyEventsSnapshot")
                val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextMyEventsCreate(lastSnapshot = lastMyEventsSnapshot)
                Log.d("MyEventsViewModel", "Received ${nextEvents.size} new events")

                if (nextEvents.isNotEmpty()) {
                    createdByMeEvents.value += nextEvents
                    lastMyEventsSnapshot = newLastSnapshot
                } else {
                    hasReachedEnd.value = true
                    Log.d("MyEventsViewModel", "No more created by me events, hasReachedEnd set to true")
                }

            } catch (e: Exception) {
                Log.e("MyEventsViewModel", "Error loading more created by me events", e)
            } finally {
                isLoadingMoreCreatedByMe.value = false
                Log.d("MyEventsViewModel", "loadMoreCreatedByMeEvents: isLoadingMore set to false")
            }
        }
        Log.d("MyEventsViewModel", "loadMoreCreatedByMeEvents: End")
    }

    fun removeUserFromEvent(eventId: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid)
        val eventDocRef = FirebaseFirestore.getInstance().collection("events").document(eventId)

        eventDocRef.update("attendees", FieldValue.arrayRemove(userDocRef))
            .addOnSuccessListener {
                Log.d("DeleteAttendee", "User successfully removed from attendees.")
            }
            .addOnFailureListener { e ->
                Log.e("DeleteAttendee", "Error removing user from attendees", e)
            }

        upcomingEvents.value = upcomingEvents.value.filterNot { it.id == eventId }
        previousEvents.value = previousEvents.value.filterNot { it.id == eventId }
        createdByMeEvents.value = createdByMeEvents.value.filterNot { it.id == eventId }
    }
}
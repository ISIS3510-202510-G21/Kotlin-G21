package com.isis3510.growhub.viewmodel

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentSnapshot
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.data.GlobalData
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.utils.ConnectionStatus
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class SearchEventViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseServicesFacade = FirebaseServicesFacade()

    val searchEvents = mutableStateOf<List<Event>>(emptyList())
    val isLoading = mutableStateOf(false)

    var categories: List<Category> = emptyList()
    var locations: List<String> = emptyList()
    var skills: List<String> = emptyList()

    private var lastSearchSnapshot: DocumentSnapshot? = null

    var searchQuery by mutableStateOf("")
    var selectedType by mutableStateOf("")
    var selectedCategory by mutableStateOf("")
    var selectedSkill by mutableStateOf("")
    var selectedLocation by mutableStateOf("")
    var selectedDate by mutableStateOf("")

    val isLoadingMore = mutableStateOf(false)
    val hasReachedEnd = mutableStateOf(false)

    private val db = AppLocalDatabase.getDatabase(application)
    private val eventRepository = EventRepository(db)
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(application)

    private val connectivityViewModel = ConnectivityViewModel(application)

    init {
        loadInitialEvents()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialEvents() {
        Log.d("SearchEventViewModel", "loadInitialEvents: Start")
        logSearchOpenedEvent()
        loadInitialSearchEvents()
        loadCategoriesSkillsLocations()
        Log.d("SearchEventViewModel", "loadInitialEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialSearchEvents() {
        Log.d("SearchEventViewModel", "loadInitialSearchEvents: Start")
        isLoading.value = true
        viewModelScope.launch {
            Log.d("SearchEventViewModel", "loadInitialSearchEvents: Calling firebaseServicesFacade.fetchSearchEvents")
            val (events, snapshot) = firebaseServicesFacade.fetchSearchEvents()
            GlobalData.searchEvents = events
            eventRepository.storeEvents(events)
            eventRepository.deleteDuplicates()
            if (events.isEmpty() || connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
                Log.d("SearchEventViewModel", "loadInitialSearchEvents: No events found, calling loadInitialSearchEventsLocal")
                isLoading.value = false
                hasReachedEnd.value = true
                loadInitialSearchEventsLocal()
                return@launch
            }
            else {
                Log.d("SearchEventViewModel", "loadInitialSearchEvents: Received ${events.size} search events from Facade")
                searchEvents.value = events
                lastSearchSnapshot = snapshot
                isLoading.value = false
                hasReachedEnd.value = events.isEmpty()
                Log.d("SearchEventViewModel", "loadInitialSearchEvents: hasReachedEnd = $hasReachedEnd")
            }
        }
        Log.d("SearchEventsViewModel", "loadInitialSearchEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialSearchEventsLocal() {
        Log.d("SearchEventViewModel", "loadInitialSearchEventsLocal: Start")
        isLoading.value = true
        viewModelScope.launch {
            Log.d("SearchEventViewModel", "loadInitialSearchEventsLocal: Calling eventRepository.getEvents")
            val events = eventRepository.getEvents(5, 0)
            GlobalData.searchEvents = events
            Log.d("SearchEventViewModel", "loadInitialSearchEventsLocal: Received ${events.size} search events from local storage")
            searchEvents.value = events
            isLoading.value = false
            hasReachedEnd.value = events.isEmpty()
            Log.d("SearchEventViewModel", "loadInitialSearchEventsLocal: hasReachedEnd = $hasReachedEnd")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreSearchEvents() {
        Log.d("SearchEventsViewModel", "loadMoreSearchEvents: Start - isLoadingMore = ${isLoadingMore.value}, hasReachedEnd = ${hasReachedEnd.value}")
        if (isLoadingMore.value || hasReachedEnd.value) return

        isLoadingMore.value = true
        viewModelScope.launch {
            try {
                Log.d("SearchEventsViewModel", "Calling firebaseServicesFacade.fetchNextSearchEvents with lastSnapshot = $lastSearchSnapshot")
                val (nextEvents, newLastSnapshot) = firebaseServicesFacade.fetchNextSearchEvents(lastSnapshot = lastSearchSnapshot)
                Log.d("SearchEventsViewModel", "Received ${nextEvents.size} new events")

                if (nextEvents.isNotEmpty()) {
                    searchEvents.value += nextEvents
                    lastSearchSnapshot = newLastSnapshot
                } else {
                    hasReachedEnd.value = true
                    Log.d("SearchEventsViewModel", "No more search events, hasReachedEnd set to true")
                }

            } catch (e: Exception) {
                Log.e("SearchEventsViewModel", "Error loading more search events", e)
            } finally {
                isLoadingMore.value = false
                Log.d("SearchEventsViewModel", "loadMoreSearchEvents: isLoadingMore set to false")
            }
        }
        Log.d("SearchEventsViewModel", "loadMoreSearchEvents: End")
    }

    private fun loadCategoriesSkillsLocations() {
        viewModelScope.launch {
            categories = firebaseServicesFacade.fetchCategories()
            locations = firebaseServicesFacade.fetchLocations()
            skills = firebaseServicesFacade.fetchSkills()
        }
    }
    val filteredEvents: List<Event>
        get() = searchEvents.value
            .filter {
                it.name.contains(searchQuery, ignoreCase = true) &&
                        (selectedType.isBlank() || (selectedType == "Free" && it.cost.toDouble() == 0.0) || (selectedType == "Paid" && it.cost > 0.0)) &&
                        (selectedCategory.isBlank() || it.category == selectedCategory) &&
                        (selectedSkill.isBlank() || it.skills.contains(selectedSkill)) &&
                        (selectedLocation.isBlank() || it.location.city == selectedLocation) &&
                        (selectedDate.isBlank() || (it.startDate >= selectedDate && it.endDate <= selectedDate))
            }

    fun clearFilters() {
        selectedType = ""
        selectedCategory = ""
        selectedSkill = ""
        selectedLocation = ""
        selectedDate = ""
        searchQuery = ""
    }

    private fun logSearchOpenedEvent() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "SearchEvents")
            putString("interaction_type", "search_screen_loaded")
        }
        firebaseAnalytics.logEvent("search_events_interaction", bundle)
    }
}

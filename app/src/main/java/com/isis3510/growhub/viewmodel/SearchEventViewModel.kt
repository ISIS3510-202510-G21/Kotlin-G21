package com.isis3510.growhub.viewmodel

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.collection.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.data.GlobalData
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.utils.SearchEventClick
import kotlinx.coroutines.flow.collectLatest
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

    private val _isOffline = mutableStateOf(false)
    val isOffline = _isOffline

    private val searchCache = LruCache<String, List<Event>>(5)

    private val SEARCH_KEY = "search_events"

    init {
        viewModelScope.launch {
            connectivityViewModel.networkStatus.collectLatest { status ->
                _isOffline.value = status == ConnectionStatus.Unavailable || status == ConnectionStatus.Lost
                loadInitialEvents()
                logSearchOpenedEvent()
                loadCategoriesSkillsLocations()
                Log.d("SearchEventsViewModel", "Network status: $status, isOffline=${_isOffline.value}")
            }
        }
    }

    private fun loadInitialEvents() {
        loadInitialSearchEvents()
    }

    private fun loadInitialSearchEvents() {
        isLoading.value = true

        val cached = searchCache[SEARCH_KEY]
        if (!cached.isNullOrEmpty()) {
            searchEvents.value = cached
            GlobalData.searchEvents = cached
            hasReachedEnd.value = false
        }

        if (_isOffline.value) {
            if (cached.isNullOrEmpty()) {
                viewModelScope.launch {
                    try {
                        val room = eventRepository.getEvents(5, 0)
                        if (room.isNotEmpty()) {
                            searchEvents.value = room
                            GlobalData.searchEvents = room
                            searchCache.put(SEARCH_KEY, room)
                        }
                        hasReachedEnd.value = room.isEmpty()
                    } catch (e: Exception) {
                        Log.e("SearchEventsViewModel", "Error loading search events from Room: ${e.message}")
                    } finally {
                        isLoading.value = false
                    }
                }
            } else {
                isLoading.value = false
            }
            return
        }

        viewModelScope.launch {
            try {
                val (events, snapshot) = firebaseServicesFacade.fetchSearchEvents()
                eventRepository.storeEvents(events)
                for (event in events) {
                    if (!GlobalData.allEvents.contains(event)) {
                        GlobalData.allEvents.add(event)
                    }
                }

                searchCache.put(SEARCH_KEY, events)

                val existingIds = searchEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                val newEvents = events.filterNot { "${it.name}-${it.startDate}" in existingIds }

                if (newEvents.isNotEmpty()) {
                    searchEvents.value += newEvents
                } else if (searchEvents.value.isEmpty()) {
                    searchEvents.value = events
                }

                GlobalData.searchEvents = searchEvents.value
                lastSearchSnapshot = snapshot
                hasReachedEnd.value = events.isEmpty()
            } catch (e: Exception) {
                Log.e("SearchEventsViewModel", "Error fetching search events: ${e.message}")
                if (searchEvents.value.isEmpty()) {
                    try {
                        val room = eventRepository.getEvents(5, 0)
                        if (room.isNotEmpty()) {
                            searchEvents.value = room
                            GlobalData.searchEvents = room
                            searchCache.put(SEARCH_KEY, room)
                        }
                        hasReachedEnd.value = room.isEmpty()
                    } catch (e: Exception) {
                        Log.e("SearchEventsViewModel", "Error loading search events from Room after Firebase failure: ${e.message}")
                    }
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadMoreSearchEvents() {
        if (isLoadingMore.value || hasReachedEnd.value) return

        if (_isOffline.value) {
            isLoadingMore.value = true
            viewModelScope.launch {
                try {
                    val currentSize = searchEvents.value.size
                    val moreEvents = eventRepository.getEvents(5, currentSize)

                    if (moreEvents.isNotEmpty()) {
                        val existingIds = searchEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                        val newEvents = moreEvents.filterNot { "${it.name}-${it.startDate}" in existingIds }

                        if (newEvents.isNotEmpty()) {
                            searchEvents.value += newEvents
                            GlobalData.searchEvents = searchEvents.value
                            searchCache.put(SEARCH_KEY, searchEvents.value)
                        } else {
                            hasReachedEnd.value = true
                        }
                    } else {
                        hasReachedEnd.value = true
                    }
                } catch (e: Exception) {
                    Log.e("SearchEventsViewModel", "Error loading more search events offline: ${e.message}")
                } finally {
                    isLoadingMore.value = false
                }
            }
            return
        }

        isLoadingMore.value = true
        viewModelScope.launch {
            try {
                val (next, newSnap) = firebaseServicesFacade.fetchNextSearchEvents(lastSnapshot = lastSearchSnapshot)

                if (next.isNotEmpty()) {
                    val existingIds = searchEvents.value.map { "${it.name}-${it.startDate}" }.toSet()
                    val newEvents = next.filterNot { "${it.name}-${it.startDate}" in existingIds }

                    if (newEvents.isNotEmpty()) {
                        searchEvents.value += newEvents
                        GlobalData.searchEvents = searchEvents.value

                        for (event in newEvents) {
                            if (!GlobalData.allEvents.contains(event)) {
                                GlobalData.allEvents.add(event)
                            }
                        }

                        eventRepository.storeEvents(newEvents)

                        searchCache.put(SEARCH_KEY, searchEvents.value)
                        lastSearchSnapshot = newSnap
                    } else {
                        hasReachedEnd.value = true
                    }
                } else {
                    hasReachedEnd.value = true
                }
            } catch (e: Exception) {
                Log.e("SearchEventsViewModel", "Error loading more search events: ${e.message}")
            } finally {
                isLoadingMore.value = false
            }
        }
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

    fun logClick(clickType: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val clickEvent = SearchEventClick(
                userId = currentUser.uid,
                clickType = clickType
            )

            FirebaseFirestore.getInstance()
                .collection("search_clicks")
                .add(clickEvent)
                .addOnSuccessListener {
                    Log.d("ClickLog", "Click logged successfully")
                }
                .addOnFailureListener {
                    Log.e("ClickLog", "Failed to log click", it)
                }
        }
    }

}
package com.isis3510.growhub.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class SearchEventViewModel : ViewModel() {

    private val firebaseServicesFacade = FirebaseServicesFacade()

    val searchEvents = mutableStateOf<List<Event>>(emptyList())
    val isLoading = mutableStateOf(false)

    var categories: List<Category> = emptyList()
    var locations: List<String> = emptyList()
    var skills: List<String> = emptyList()

    var searchQuery by mutableStateOf("")
    var selectedType by mutableStateOf("")
    var selectedCategory by mutableStateOf("")
    var selectedSkill by mutableStateOf("")
    var selectedLocation by mutableStateOf("")
    var selectedDate by mutableStateOf("")

    val isLoadingMore = mutableStateOf(false)
    val hasReachedEnd = mutableStateOf(false)

    private var currentEventIds: List<String> = emptyList()
    private var eventsOffset: Long = 0

    init {
        loadInitialEvents()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadInitialEvents() {
        Log.d("SearchEventViewModel", "loadInitialEvents: Start")
        loadInitialSearchEvents()
        Log.d("SearchEventViewModel", "loadInitialEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadInitialSearchEvents() {
        Log.d("SearchEventsViewModel", "loadInitialSearchEvents: Start")
        isLoading.value = true
        viewModelScope.launch {
            Log.d("SearchEventsViewModel", "loadInitialSearchEvents: Calling firebaseServicesFacade.fetchSearchEvents")
            val events = firebaseServicesFacade.fetchSearchEvents()
            Log.d("SearchEventsViewModel", "loadInitialSearchEvents: Received ${events.size} search events from Facade")
            searchEvents.value = events
            isLoading.value = false
            hasReachedEnd.value = events.isEmpty()
            Log.d("SearchEventsViewModel", "loadInitialSearchEvents: hasReachedEnd = $hasReachedEnd")
            categories = firebaseServicesFacade.fetchCategories()
            locations = firebaseServicesFacade.fetchLocations()
            skills = firebaseServicesFacade.fetchSkills()
        }
        Log.d("SearchEventsViewModel", "loadInitialSearchEvents: End")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoreSearchEvents() {
        Log.d("SearchEventsViewModel", "loadMoreSearchEvents: Start - isLoadingMore = ${isLoadingMore.value}, hasReachedEnd = ${hasReachedEnd.value}")
        if (isLoadingMore.value || hasReachedEnd.value) return

        isLoadingMore.value = true
        viewModelScope.launch {
            val alreadyLoadedIds = searchEvents.value.map { it.id }
            Log.d("SearchEventsViewModel", "loadMoreSearchEvents: Calling firebaseServicesFacade.fetchNextSearchEvents, excluding ${alreadyLoadedIds.size} IDs")
            val nextEvents = firebaseServicesFacade.fetchNextSearchEvents(excludedIds = alreadyLoadedIds)
            Log.d("SearchEventsViewModel", "loadMoreSearchEvents: Received ${nextEvents.size} next search events from Facade")
            if (nextEvents.isNotEmpty()) {
                searchEvents.value += nextEvents
            } else {
                hasReachedEnd.value = true
                Log.d("SearchEventsViewModel", "loadMoreSearchEvents: No more search events, hasReachedEnd set to true")
            }
            isLoadingMore.value = false
            Log.d("SearchEventsViewModel", "loadMoreSearchEvents: isLoadingMore set to false")
            categories = firebaseServicesFacade.fetchCategories()
            locations = firebaseServicesFacade.fetchLocations()
            skills = firebaseServicesFacade.fetchSkills()
        }
        Log.d("SearchEventsViewModel", "loadMoreEvents: End")
    }

    val filteredEvents: List<Event>
        get() = searchEvents.value
            .filter {
                it.name.contains(searchQuery, ignoreCase = true) &&
                        (selectedType.isBlank() || (selectedType == "Free" && it.cost.toDouble() == 0.0) || (selectedType == "Paid" && it.cost > 0.0)) &&
                        (selectedCategory.isBlank() || it.category == selectedCategory) &&
                        (selectedSkill.isBlank() || it.skills.contains(selectedSkill)) &&
                        (selectedLocation.isBlank() || it.location == selectedLocation) &&
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
}

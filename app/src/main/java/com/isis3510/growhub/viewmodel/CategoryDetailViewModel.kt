package com.isis3510.growhub.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.local.data.GlobalData
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CategoryDetailViewModel : ViewModel() {

    private val _categoryEvents = mutableStateOf<List<Event>>(emptyList())
    val categoryEvents: State<List<Event>> = _categoryEvents

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore

    val hasReachedEnd = mutableStateOf(false)
    private var currentPage = 0
    private val pageSize = 10

    private val _categoryName = mutableStateOf("")
    val categoryName: State<String> = _categoryName

    // Selected filters
    var selectedType = mutableStateOf("")
    var selectedSorting = mutableStateOf(EventsSorting.SOONEST_TO_LATEST.name)

    // Filtered events
    private val _filteredEvents = mutableStateOf<List<Event>>(emptyList())
    val filteredEvents: State<List<Event>> = _filteredEvents

    private val clickStats = mutableMapOf<String, Int>()

    /**
     * Initialize the view model with the category name
     */
    fun initialize(categoryName: String) {
        _categoryName.value = categoryName
        loadInitialEvents()
    }

    /**
     * Load initial events from GlobalData filtered by category
     */
    private fun loadInitialEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Filter all events by the category name
                val categoryFilteredEvents = GlobalData.allEvents.filter {
                    it.category == _categoryName.value
                }

                _categoryEvents.value = categoryFilteredEvents
                currentPage = 0
                hasReachedEnd.value = false

                // Initial filter application
                applyFilters()
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading events: ${e.message}")
                _categoryEvents.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load more events for pagination
     */
    fun loadMoreCategoryEvents() {
        if (_isLoadingMore.value || hasReachedEnd.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            try {
                val startIndex = (currentPage + 1) * pageSize
                if (startIndex >= _filteredEvents.value.size) {
                    hasReachedEnd.value = true
                } else {
                    currentPage++

                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error loading more events: ${e.message}")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Apply filters to the events list
     */
    fun applyFilters() {
        viewModelScope.launch {
            val result = _categoryEvents.value.filter { event ->
                val typeMatches = selectedType.value.isEmpty() ||
                        (selectedType.value == "Free" && event.cost == 0) ||
                        (selectedType.value == "Paid" && event.cost != 0)

                typeMatches
            }

            // Apply sorting
            val sortedResult = when (selectedSorting.value) {
                EventsSorting.SOONEST_TO_LATEST.name -> {
                    result.sortedBy { parseDate(it.startDate) }
                }
                EventsSorting.LATEST_TO_SOONEST.name -> {
                    result.sortedByDescending { parseDate(it.startDate) }
                }
                else -> result
            }

            _filteredEvents.value = sortedResult
            currentPage = 0
            hasReachedEnd.value = _filteredEvents.value.size <= pageSize
        }
    }

    /**
     * Parse date string to LocalDate
     */
    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        selectedType.value = ""
        selectedSorting.value = EventsSorting.SOONEST_TO_LATEST.name
        applyFilters()
    }

    /**
     * Log button click for analytics
     */
    fun logClick(buttonId: String) {
        val currentCount = clickStats.getOrDefault(buttonId, 0)
        clickStats[buttonId] = currentCount + 1
        Log.d("CategoryViewModel", "Button $buttonId clicked. Total: ${clickStats[buttonId]}")
    }
}

/**
 * Enum class for event sorting options
 */
enum class EventsSorting {
    SOONEST_TO_LATEST,
    LATEST_TO_SOONEST;

    /**
     * Get a user-friendly display name for the sorting option
     */
    fun getDisplayName(): String {
        return when (this) {
            SOONEST_TO_LATEST -> "Soonest to Latest"
            LATEST_TO_SOONEST -> "Latest to Soonest"
        }
    }
}
package com.isis3510.growhub.viewmodel

import android.os.Build
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

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
class SearchEventViewModel (
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    var searchQuery by mutableStateOf("")
    var selectedType by mutableStateOf("")
    var selectedCategory by mutableStateOf("")
    var selectedSkill by mutableStateOf("")
    var selectedLocation by mutableStateOf("")
    var selectedDate by mutableStateOf("")

    var allEvents by mutableStateOf(listOf<Event>())
    var categories by mutableStateOf(listOf<Category>())
    var skills by mutableStateOf(listOf<String>())
    var locations by mutableStateOf(listOf<String>())

    init {
        loadEventsFromFirebase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadEventsFromFirebase() {
        viewModelScope.launch {
            allEvents = firebaseFacade.fetchAllEvents()
            categories = firebaseFacade.fetchCategories()
            locations = firebaseFacade.fetchLocations()
            skills = firebaseFacade.fetchSkills()
        }
    }

    val filteredEvents: List<Event>
        get() = allEvents.filter {
            it.name.contains(searchQuery, ignoreCase = true) &&
                    // check if the event is free or paid
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
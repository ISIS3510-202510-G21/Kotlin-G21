package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val upcomingEvents = mutableStateListOf<Event>()
    val nearbyEvents = mutableStateListOf<Event>()
    val recommendedEvents = mutableStateListOf<Event>()
    val categories = mutableStateListOf<Category>()

    init {
        loadMockEvents()
        loadMockCategories()
    }

    private fun loadMockEvents() {
        viewModelScope.launch {
            val mockData = listOf(
                Event("1", "El Riqué (México) 5to Cir...", "Bogotá, Colombia", "February 26, 2025", "Music", "mock_image", 100.0),
                Event("2", "IEEE Zona Centro", "Bogotá, Colombia", "March 1, 2025", "Technology", "mock_image", 0.0),
                Event("3", "Taller Entrevista", "Bogotá, Colombia", "March 4, 2025", "Business", "mock_image", 10.0),
                Event("4", "XXIV Jornadas C...", "Bogotá, Colombia", "February 25, 2025", "Science", "mock_image", 50.0)
            )
            upcomingEvents.addAll(mockData.take(2))
            recommendedEvents.addAll(mockData.drop(1))
            nearbyEvents.addAll(mockData)
        }
    }

    private fun loadMockCategories() {
        viewModelScope.launch {
            categories.addAll(Category.entries.toTypedArray())
        }
    }
}

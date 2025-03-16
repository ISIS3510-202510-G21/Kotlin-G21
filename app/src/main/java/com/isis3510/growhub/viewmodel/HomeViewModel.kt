package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Modelo de datos
data class Event(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val category: String,
    val imageUrl: String
)

class HomeViewModel : ViewModel() {
    val upcomingEvents = mutableStateListOf<Event>()
    val nearbyEvents = mutableStateListOf<Event>()
    val recommendedEvents = mutableStateListOf<Event>()

    init {
        loadMockEvents()
    }

    private fun loadMockEvents() {
        viewModelScope.launch {
            val mockData = listOf(
                Event("1", "El Riqué (México) 5to Cir...", "Bogotá, Colombia", "February 26, 2025", "Music", "mock_image"),
                Event("2", "IEEE Zona Centro", "Bogotá, Colombia", "March 1, 2025", "Technology", "mock_image"),
                Event("3", "Taller Entrevista", "Bogotá, Colombia", "March 4, 2025", "Business", "mock_image"),
                Event("4", "XXIV Jornadas C...", "Bogotá, Colombia", "February 25, 2025", "Science", "mock_image")
            )
            upcomingEvents.addAll(mockData.take(2))
            nearbyEvents.addAll(mockData.drop(2))
            recommendedEvents.addAll(mockData)
        }
    }
}

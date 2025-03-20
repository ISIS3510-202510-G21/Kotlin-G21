package com.isis3510.growhub.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    val nearbyEvents = mutableStateListOf<Event>()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> get() = _userLocation

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = LatLng(lat, lng)
    }

    init {
        loadNearbyEvents()
    }

    private fun loadNearbyEvents() {
        viewModelScope.launch {
            val mockEvents = listOf(
                Event("5", "Festival de Jazz", "Medellín, Colombia", "April 10, 2025", "Music", "mock_image", 50.0),
                Event("6", "Hackathon AI", "Bogotá, Colombia", "April 15, 2025", "Technology", "mock_image", 0.0),
                Event("7", "Cuidemos el planeta", "Cali, Colombia", "April 17, 2025", "Environment", "mock_image", 10.0)
            )
            nearbyEvents.addAll(mockEvents)
        }
    }
}

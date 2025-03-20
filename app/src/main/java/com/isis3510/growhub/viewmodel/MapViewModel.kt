package com.isis3510.growhub.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel: ViewModel() {
    val nearbyEvents = mutableStateListOf<Event>()

    // State to hold the user's location as LatLng (latitude and longitude)
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation


    // Function to fetch the user's location and update the state
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Fetch the last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        // Update the user's location in the state
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                    }
                }
            } catch (e: SecurityException) {
                Log.e("MapViewModel", "Permission for location access was revoked: ${e.localizedMessage}")
            }
        } else {
            Log.e("MapViewModel", "Location permission is not granted.")
        }
    }

    init {
        loadNearbyEvents()
    }

    private fun loadNearbyEvents() {
        // Carga manualmente los eventos según los datos que proporcionaste
        nearbyEvents.clear()
        nearbyEvents.addAll(
            listOf(
                Event("5", "Festival de Jazz", "Medellín, Colombia", "April 10, 2025", "Music", "mock_image", 50.0),
                Event("6", "Hackathon AI", "Bogotá, Colombia", "April 15, 2025", "Technology", "mock_image", 0.0),
                Event("7", "Cuidemos el planeta", "Cali, Colombia", "April 17, 2025", "Environment", "mock_image", 10.0)
            )
        )
    }
}






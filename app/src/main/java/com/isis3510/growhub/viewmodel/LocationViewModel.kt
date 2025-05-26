package com.isis3510.growhub.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.net.HttpURLConnection.HTTP_OK

sealed interface SimpleLocationUiState {
    object Idle : SimpleLocationUiState
    object Loading : SimpleLocationUiState
    data class Success(val rawResponse: String) : SimpleLocationUiState
    data class Error(val message: String) : SimpleLocationUiState
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _locationState = MutableStateFlow<SimpleLocationUiState>(SimpleLocationUiState.Idle)
    val locationState: StateFlow<SimpleLocationUiState> = _locationState.asStateFlow()

    private var locationListener: LocationListener? = null

    init {
        logLastSavedCoordinates()
        refreshLocation()
    }

    private fun logLastSavedCoordinates() {
        val lat = prefs.getString("user_latitude", null)
        val lon = prefs.getString("user_longitude", null)
        val source = prefs.getString("location_source", "Unknown")
        Log.d("LocationViewModel", "Last saved coords: $lat, $lon (source: $source)")
    }

    fun refreshLocation() {
        if (_locationState.value is SimpleLocationUiState.Loading) return
        _locationState.update { SimpleLocationUiState.Loading }
        if (hasLocationPermission()) fetchGPSLocation() else fetchLocationFromIpApi()
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun fetchGPSLocation() {
        // remove previous listener
        locationListener?.let { locationManager.removeUpdates(it) }

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                saveAndReverse(location)
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                Log.w("LocationViewModel", "GPS disabled, fallback to IP")
                fetchLocationFromIpApi()
            }
        }

        try {
            // first try last known
            val last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (last != null && System.currentTimeMillis() - last.time < 5 * 60_000) {
                saveAndReverse(last)
                return
            }

            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> {
                    fetchLocationFromIpApi()
                    return
                }
            }

            // request updates on main looper
            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                locationListener!!,
                Looper.getMainLooper()
            )

            // timeout: fallback if no location
            viewModelScope.launch {
                delay(10_000)
                if (_locationState.value is SimpleLocationUiState.Loading) {
                    locationManager.removeUpdates(locationListener!!)
                    Log.w("LocationViewModel", "GPS timeout, fallback to IP")
                    fetchLocationFromIpApi()
                }
            }

        } catch (e: SecurityException) {
            Log.e("LocationViewModel", "No permission for GPS", e)
            fetchLocationFromIpApi()
        }
    }

    private fun saveAndReverse(location: Location) {
        prefs.edit()
            .putString("user_latitude", location.latitude.toString())
            .putString("user_longitude", location.longitude.toString())
            .putString("location_source", "GPS")
            .apply()
        reverseGeocode(location.latitude, location.longitude)
    }

    private fun reverseGeocode(lat: Double, lon: Double) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://ip-api.com/json/?lat=$lat&lon=$lon")
                    (url.openConnection() as HttpURLConnection).run {
                        requestMethod = "GET"
                        connectTimeout = 8000; readTimeout = 8000; connect()
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            inputStream.bufferedReader().use { it.readText() }
                        } else "{\"status\":\"fail\"}"
                    }
                } catch (e: Exception) {"{\"status\":\"fail\"}"}
            }
            _locationState.value = SimpleLocationUiState.Success(response)
        }
    }

    private fun fetchLocationFromIpApi() {
        viewModelScope.launch {
            _locationState.update { SimpleLocationUiState.Loading }
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://ip-api.com/json/")
                    (url.openConnection() as HttpURLConnection).run {
                        requestMethod = "GET"
                        connect(); if (responseCode == HTTP_OK)
                        inputStream.bufferedReader().use { it.readText() }
                    else throw Exception("HTTP $responseCode")
                    }
                } catch (e: Exception) { return@withContext SimpleLocationUiState.Error(e.message ?: "Error") }
            }
            if (result is String) _locationState.value = SimpleLocationUiState.Success(result)
        }
    }

    /** Para recuperar las coordenadas más adelante: */
    fun getLastKnownLatLng(): Pair<Double?, Double?> {
        val lat = prefs.getString("user_latitude", null)?.toDoubleOrNull()
        val lon = prefs.getString("user_longitude", null)?.toDoubleOrNull()
        return Pair(lat, lon)
    }

    override fun onCleared() {
        super.onCleared()
        locationListener?.let { try { locationManager.removeUpdates(it) } catch (_: Exception) {} }
    }
}

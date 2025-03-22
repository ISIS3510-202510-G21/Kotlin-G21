package com.isis3510.growhub.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class NearbyEventsViewModel(manifestApiKey: String?): ViewModel() {
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation

    /*
    private val _mapEvents = MutableStateFlow<List<LatLng>>(emptyList())
    val mapEvents: StateFlow<List<LatLng>> = _mapEvents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
        fetchEventsFromAPI()
    }

    // Extract the API Key
    private val apiKey = manifestApiKey

    private val client = OkHttpClient()
    fun fetchEventsFromAPI() {
        viewModelScope.launch {
            Log.d("Lanzamiento", "Se lanzó fetch")
            val results = mutableListOf<LatLng>()

            for (event in nearbyEvents) {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val encodedAddress = URLEncoder.encode(event.location, "UTF-8")
                        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$apiKey"
                        val request = Request.Builder()
                            .url(url)
                            .build()
                        // Usa "client" (no okHttpClient) para ejecutar la solicitud
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                Log.e("API", "Error HTTP: ${response.code} - ${event.location}")
                                return@withContext null
                            }

                            response.body?.string()?.let { body ->
                                val json = JSONObject(body)
                                when (json.getString("status")) {
                                    "OK" -> {
                                        val location = json.getJSONArray("results")
                                            .getJSONObject(0)
                                            .getJSONObject("geometry")
                                            .getJSONObject("location")
                                        LatLng(
                                            location.getDouble("lat"),
                                            location.getDouble("lng")
                                        )
                                    }
                                    else -> {
                                        Log.e("API", "Estado no OK: ${json.getString("status")} - ${event.location}")
                                        null
                                    }
                                }
                            } ?: run {
                                Log.e("API", "Cuerpo vacío - ${event.location}")
                                null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("API", "Excepción en: ${event.location}", e)
                        null
                    }
                }
                result?.let { results.add(it) }
            }
            _mapEvents.value = results
        }
    }

    // Used to store the nearby events as well as the Geocode API conversion to LatLng
    val nearbyEvents = mutableStateListOf<Event>()

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
*/
}
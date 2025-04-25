package com.isis3510.growhub.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

@RequiresApi(Build.VERSION_CODES.O)
class MapViewModel(
        private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
    ): ViewModel() {

    val nearbyEvents = mutableStateListOf<Event>()

    // State to hold the user's location as LatLng (latitude and longitude)
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    private var approxCity: String = ""

    // Function to fetch the user's location and update the state
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                    } ?: run {
                        Log.e("MapViewModel", "No last known location available.")
                    }
                }
            } catch (e: SecurityException) {
                Log.e("MapViewModel", "Location access permission error: ${e.localizedMessage}")
            }
        } else {
            Log.e("MapViewModel", "Location permission is not granted. Using approximate location.")
        }
    }

    private suspend fun fetchApproximateCity(): String? = suspendCancellableCoroutine { continuation ->
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://ip-api.com/json/")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapViewModel", "Failed to get approximate city: ${e.localizedMessage}")
                if (!continuation.isCompleted) {
                    continuation.resume(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val city = jsonObject.getString("city")
                        if (!continuation.isCompleted) {
                            continuation.resume(city)
                        }
                    } catch (e: JSONException) {
                        Log.e("MapViewModel", "Error parsing approximate city: ${e.localizedMessage}")
                        if (!continuation.isCompleted) {
                            continuation.resume(null)
                        }
                    }
                } ?: run {
                    if (!continuation.isCompleted) {
                        continuation.resume(null)
                    }
                }
            }
        })
    }

    init {
        viewModelScope.launch {
            val city = fetchApproximateCity()
            if (city.isNullOrEmpty()) {
                Log.e("MapViewModel", "Not able to get approximate city.")
                return@launch
            }
            // Assign the city to the approx variable
            approxCity = city

            // Load events once and only once the asynchronous fetch has returned
            loadNearbyEventsFromFirebase()
        }
    }

    private fun loadNearbyEventsFromFirebase() {
        viewModelScope.launch {
            try {
                val (events, _) = firebaseFacade.fetchHomeEvents() // Unpack the pair, ignore lastSnapshot

                // Filter events whose location city is the same as the approx location city
                val filteredEvents = events.filter { event ->
                    val locationParts = event.location.city.split(",")
                    val locationCity = locationParts.getOrNull(1)?.trim() ?: ""
                    locationCity.equals(approxCity, ignoreCase = true)
                }

                // Update the mutableStateListOf with the filtered events
                nearbyEvents.clear()
                nearbyEvents.addAll(filteredEvents)

            } catch (e: Exception) {
                Log.e("NearbyEvents", "Error loading nearby events", e)
            }
        }
    }
}
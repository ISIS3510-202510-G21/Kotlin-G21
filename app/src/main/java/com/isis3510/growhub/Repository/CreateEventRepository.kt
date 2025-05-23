package com.isis3510.growhub.Repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class CreateEventRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "CreateEventRepo"

    // Esta clase mantendrá los resultados de geocodificación
    data class GeocodingResult(
        val isValid: Boolean,
        val formattedAddress: String?,
        val latitude: Double?,
        val longitude: Double?,
        val errorMessage: String?
    )

    // Función modificada para usar Geocoder de Android
    suspend fun validateAndGeocodeAddress(address: String): GeocodingResult {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // Uso de getFromLocationName
                val addresses = geocoder.getFromLocationName(address, 1)

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    Log.d("Geocode Address", "${location.latitude}, ${location.longitude}")
                    GeocodingResult(
                        isValid = true,
                        formattedAddress = address,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        errorMessage = null
                    )
                } else {
                    Log.d("Geocode Address", "No address found")
                    GeocodingResult(
                        isValid = false,
                        formattedAddress = null,
                        latitude = null,
                        longitude = null,
                        errorMessage = "No se encontró la dirección"
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error al geocodificar dirección", e)
                GeocodingResult(
                    isValid = false,
                    formattedAddress = null,
                    latitude = null,
                    longitude = null,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    suspend fun createEvent(
        name: String,
        cost: Int,
        category: String,
        description: String,
        startDate: Timestamp,
        endDate: Timestamp,
        locationId: String,
        imageUrl: String?,
        address: String,
        details: String,
        city: String,
        isUniversity: Boolean,
        skillIds: List<String>,
        latitude: Double? = null,
        longitude: Double? = null
    ): String? {
        Log.d(TAG, "Grow Category received = '$category'")

        val categoryQuerySnapshot = firestore.collection("categories")
            .whereEqualTo("name", category.trim())
            .get()
            .await()

        Log.d(TAG, "Matches found = ${categoryQuerySnapshot.size()}")

        if (categoryQuerySnapshot.isEmpty) {
            Log.e(TAG, "Category NOT found in Firestore.")
            return null
        }
        val categoryRef = categoryQuerySnapshot.documents[0].reference

        // Add coordinates to location if available
        var coordLat = 0.00
        var coordLong = 0.00

        if (latitude != null && longitude != null) {
            coordLat = latitude
            coordLong = longitude
        }

        // Enhanced location data with latitude and longitude if available
        val locationData = hashMapOf(
            "address" to address,
            "latitude" to coordLat,
            "longitude" to coordLong,
            "city" to city,
            "details" to details,
            "university" to isUniversity
        )

        val locationRef: DocumentReference = firestore.collection("locations")
            .add(locationData)
            .await()

        val finalImageUrl = imageUrl
            ?: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQKF_YlFFlKS6AQ8no0Qs_xM6AkjvwFwP61og&s"

        val emptyAttendeesList = arrayListOf<String>()

        val skillRefs = skillIds.map { skillId ->
            firestore.collection("skills").document(skillId)
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = firestore.collection("users").document(uid ?: "")

        val eventData = hashMapOf(
            "name" to name,
            "cost" to cost,
            "category" to categoryRef,
            "description" to description,
            "start_date" to startDate,
            "end_date" to endDate,
            "location_id" to locationRef,
            "image" to finalImageUrl,
            "users_registered" to 0,
            "skills" to skillRefs,
            "attendees" to emptyAttendeesList,
            "creator_id" to userRef
        )

        val eventRef = firestore.collection("events")
            .add(eventData)
            .await()

        return eventRef.id
    }
}
package com.isis3510.growhub.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CreateEventRepository {

    private val firestore = FirebaseFirestore.getInstance()
    suspend fun createEvent(
        name: String,
        cost: Double,
        category: String,
        description: String,
        startDate: Timestamp,
        endDate: Timestamp,
        locationId: String,
        imageUrl: String?,
        address: String,
        details: String
    ): Boolean {
        val finalImageUrl = imageUrl ?: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQKF_YlFFlKS6AQ8no0Qs_xM6AkjvwFwP61og&s"
        val emptyUsersList = arrayListOf<String>()

        val locationData = hashMapOf(
            "address" to address,
            "city" to "Bogotá",
            "details" to details,
            "university" to true,
        )

        val locationRef: DocumentReference = firestore.collection("locations")
            .add(locationData)
            .await()


        val eventData = hashMapOf(
            "name" to name,
            "cost" to cost,
            "category" to category,
            "description" to description,
            "start_date" to startDate,
            "end_date" to endDate,
            "location_id" to locationRef,     // String con formato "/locations/..."
            "image" to finalImageUrl,        // string
            "users_registered" to emptyUsersList
        )

        firestore.collection("events")
            .add(eventData)
            .await()

        return true
    }

}

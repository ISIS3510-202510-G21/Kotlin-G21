package com.isis3510.growhub.Repository

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
        details: String,
        city: String,
        isUniversity: Boolean,
        skillIds: List<String>
    ): Boolean {
        // Primero buscamos la categoría correspondiente en Firestore
        val categoryQuerySnapshot = firestore.collection("categories")
            .whereEqualTo("name", category)
            .get()
            .await()

        if (categoryQuerySnapshot.isEmpty) {
            return false
        }
        val categoryRef = categoryQuerySnapshot.documents[0].reference

        val locationData = hashMapOf(
            "address" to address,
            "city" to city,
            "details" to details,
            "university" to isUniversity
        )

        val locationRef: DocumentReference = firestore.collection("locations")
            .add(locationData)
            .await()

        val finalImageUrl = imageUrl
            ?: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQKF_YlFFlKS6AQ8no0Qs_xM6AkjvwFwP61og&s"

        val emptyUsersList = arrayListOf<String>()

        val skillRefs = skillIds.map { skillId ->
            firestore.collection("skills").document(skillId)
        }

        val eventData = hashMapOf(
            "name" to name,
            "cost" to cost,
            "category" to categoryRef,
            "description" to description,
            "start_date" to startDate,
            "end_date" to endDate,
            "location_id" to locationRef,
            "image" to finalImageUrl,
            "users_registered" to emptyUsersList,
            "skills" to skillRefs
        )

        firestore.collection("events")
            .add(eventData)
            .await()

        return true
    }
}

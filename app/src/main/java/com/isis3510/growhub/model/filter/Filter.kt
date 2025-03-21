package com.isis3510.growhub.model.filter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Created by: Juan Manuel Jáuregui
 */

class Filter(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getProfileData(): Map<String, Any>? {
        val userId = auth.currentUser?.uid ?: return null

        val userDocRef = db.collection("users").document(userId)

        val querySnapshot = db.collection("profiles")
            .whereEqualTo("user_ref", userDocRef)
            .get()
            .await()

        val profileDocument = querySnapshot.documents.firstOrNull()

        val profileData = profileDocument?.data ?: emptyMap()

        return mapOf(
            "profilePicture" to profileData["profile_picture"] as String,
            "description" to profileData["description"] as String,
            "interests" to (profileData["interests"] as List<DocumentReference>),
            "followers" to (profileData["followers"] as List<DocumentReference>),
            "following" to (profileData["following"] as List<DocumentReference>),
            "user_ref" to profileData["user_ref"] as DocumentReference
        )
    }

    suspend fun getEventsData(): List<Map<String, Any>> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val querySnapshot = db.collection("events")
            .get()
            .await()

        val attendedEvents = mutableListOf<Map<String, Any>>()

        for (eventDocument in querySnapshot.documents) {
            val attendees = eventDocument.get("attendees") as? List<DocumentReference> ?: emptyList()

            // Check if the user is in the attendees list
            if (attendees.any { it.id == userId }) {
                attendedEvents.add(eventDocument.data ?: emptyMap())
            }
        }

        return attendedEvents
    }

    suspend fun getHomeEventsData(): List<Map<String, Any>> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val querySnapshot = db.collection("events")
            .get()
            .await()

        val events = mutableListOf<Map<String, Any>>()

        for (eventDocument in querySnapshot.documents) {
            events.add(eventDocument.data ?: emptyMap())
        }

        return events
    }

    suspend fun getCategoriesData(): List<Map<String, Any>> {
        val querySnapshot = db.collection("categories")
            .get()
            .await()

        val categories = mutableListOf<Map<String, Any>>()

        for (categoryDocument in querySnapshot.documents) {
            categories.add(categoryDocument.data ?: emptyMap())
        }

        return categories

    }

}
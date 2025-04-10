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

        val querySnapshot = db.collection("events")
            .get()
            .await()

        val events = mutableListOf<Map<String, Any>>()

        for (eventDocument in querySnapshot.documents) {
            events.add(eventDocument.data ?: emptyMap())
        }

        return events
    }

    suspend fun getHomeRecommendedEventsData(): List<Map<String, Any>> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val querySnapshot = db.collection("recommendations")
            .get()
            .await()

        // Get the document that matches the user ID
        val recommendationDocument = querySnapshot.documents.firstOrNull { it.id == userId } ?: return emptyList()

        val events = mutableListOf<Map<String, Any>>()
        val recommendedEvents = recommendationDocument.get("events") as? List<String> ?: emptyList()
        for (eventId in recommendedEvents) {
            val eventDocument = db.collection("events").document(eventId).get().await()
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

    suspend fun getRegistrationData(eventID: String): Map<String, Any> {
        val eventDocRef = db.collection("events").document(eventID)
        val eventDoc = eventDocRef.get().await()
        val eventData = eventDoc.data ?: emptyMap()

        return mapOf(
            "attendees" to (eventData["attendees"] as List<DocumentReference>),
            "category" to (eventData["category"] as DocumentReference),
            "cost" to (eventData["cost"] as Long).toInt(),
            "creator_id" to (eventData["creator_id"] as DocumentReference),
            "description" to (eventData["description"] as String),
            "end_date" to (eventData["end_date"] as com.google.firebase.Timestamp),
            "image" to (eventData["image"] as String),
            "location_id" to (eventData["location_id"] as DocumentReference),
            "name" to (eventData["name"] as String),
            "skills" to (eventData["skills"] as List<DocumentReference>),
            "start_date" to (eventData["start_date"] as com.google.firebase.Timestamp),
            "users_registered" to (eventData["users_registered"] as Long).toInt()
        )
    }

}
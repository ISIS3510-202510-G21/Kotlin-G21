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
        // Recupera el id del usuario actualmente autenticado
        val userId = auth.currentUser?.uid ?: return emptyList()

        // Obtiene el documento del usuario desde la colección "users"
        val querySnapshot = db.collection("users").document(userId)
            .get()
            .await()

        // Extrae la lista de ids de eventos recomendados del atributo "recommended_events"
        val recommendedEventsIds = querySnapshot.get("recommended_events") as? List<String> ?: emptyList()

        // Lista para acumular los datos de los eventos
        val events = mutableListOf<Map<String, Any>>()

        // Para cada id, consulta el documento correspondiente en la colección "events"
        for (eventId in recommendedEventsIds) {
            val eventDocument = db.collection("events").document(eventId).get().await()
            // Si el documento existe, añade sus datos a la lista
            eventDocument.data?.let { events.add(it) }
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
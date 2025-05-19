package com.isis3510.growhub.model.filter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isis3510.growhub.utils.ProfileCache
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

        val cachedProfile = ProfileCache.get("user_profile")
        if (cachedProfile != null) {
            return cachedProfile
        }

        val userDocRef = db.collection("users").document(userId)

        val querySnapshot = db.collection("profiles")
            .whereEqualTo("user_ref", userDocRef)
            .get()
            .await()

        val profileDocument = querySnapshot.documents.firstOrNull()
        val profileData = profileDocument?.data ?: emptyMap()

        val interestsRefs = profileData["interests"] as? List<DocumentReference> ?: emptyList()
        val followersRefs = profileData["followers"] as? List<DocumentReference> ?: emptyList()
        val followingRefs = profileData["following"] as? List<DocumentReference> ?: emptyList()

        val interestsNames = interestsRefs.mapNotNull {
            it.get().await().getString("name")
        }

        val userName = userDocRef.get().await().getString("name") ?: ""

        val result = mapOf(
            "profilePicture" to (profileData["profile_picture"] as? String ?: ""),
            "description" to (profileData["description"] as? String ?: ""),
            "headline" to (profileData["headline"] as? String ?: ""),
            "interests" to interestsNames,
            "followers" to followersRefs.size,
            "following" to followingRefs.size,
            "name" to userName
        )

        ProfileCache.put("user_profile", result)
        Log.d("Filter", "Profile data cached")
        Log.d("Filter", "Profile data: $result")

        return result
    }

    suspend fun getMyEventsData(limit: Long): Pair<List<Map<String, Any>>, DocumentSnapshot?> {
        val userId = auth.currentUser?.uid ?: return Pair(emptyList(), null)

        Log.d("MyEvents", "Query for more events")
        val querySnapshot = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val filteredEvents = events.filter { event ->
            val attendees = event["attendees"] as? List<DocumentReference>
            attendees?.any { attendeeRef -> attendeeRef.id == userId } ?: false
        }

        val lastSnapshot = querySnapshot.documents.lastOrNull()

        return Pair(filteredEvents, lastSnapshot)
    }

    suspend fun getNextMyEventsData(
        limit: Long = 3,
        lastDocumentSnapshot: DocumentSnapshot? = null
    ): Pair<List<Map<String, Any>>, DocumentSnapshot?> {
        val userId = auth.currentUser?.uid ?: return Pair(emptyList(), null)

        Log.d("MyEvents", "Querying next events with limit = $limit")

        val baseQuery = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)

        val query = lastDocumentSnapshot?.let {
            baseQuery.startAfter(it)
        } ?: baseQuery

        val querySnapshot = query.get().await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val filteredEvents = events.filter { event ->
            val attendees = event["attendees"] as? List<DocumentReference>
            attendees?.any { attendeeRef -> attendeeRef.id == userId } ?: false
        }

        val newLastSnapshot = querySnapshot.documents.lastOrNull()

        Log.d("MyEvents", "Fetched ${filteredEvents.size} events")

        return Pair(filteredEvents, newLastSnapshot)
    }

    suspend fun getMyEventsCreateData(limit: Long): Pair<List<Map<String, Any>>, DocumentSnapshot?> {
        val userId = auth.currentUser?.uid ?: return Pair(emptyList(), null)

        Log.d("MyEvents", "Query for more events")
        val querySnapshot = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val filteredEvents = events.filter { event ->
            val creatorRef = event["creator_id"] as? DocumentReference
            creatorRef?.id == userId
        }

        val lastSnapshot = querySnapshot.documents.lastOrNull()

        return Pair(filteredEvents, lastSnapshot)
    }

    suspend fun getNextMyEventsCreateData(
        limit: Long = 3,
        lastDocumentSnapshot: DocumentSnapshot? = null
    ): Pair<List<Map<String, Any>>, DocumentSnapshot?> {
        val userId = auth.currentUser?.uid ?: return Pair(emptyList(), null)

        Log.d("MyEvents", "Querying next events with limit = $limit")

        val baseQuery = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)

        val query = lastDocumentSnapshot?.let {
            baseQuery.startAfter(it)
        } ?: baseQuery

        val querySnapshot = query.get().await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val filteredEvents = events.filter { event ->
            val creatorRef = event["creator_id"] as? DocumentReference
            creatorRef?.id == userId
        }

        val newLastSnapshot = querySnapshot.documents.lastOrNull()

        Log.d("MyEvents", "Fetched ${filteredEvents.size} events")

        return Pair(filteredEvents, newLastSnapshot)
    }

    suspend fun getHomeEventsData(limit: Long): Pair<List<MutableMap<String, Any>>, DocumentSnapshot?> {

        Log.d("HomeEvents", "Query for more events")
        val querySnapshot = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val lastSnapshot = querySnapshot.documents.lastOrNull()

        return Pair(events, lastSnapshot)
    }

    suspend fun getNextHomeEventsData(
        limit: Long = 3,
        lastDocumentSnapshot: DocumentSnapshot? = null
    ): Pair<List<Map<String, Any>>, DocumentSnapshot?> {

        Log.d("HomeEvents", "Querying next events with limit = $limit")

        val baseQuery = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)

        val query = lastDocumentSnapshot?.let {
            baseQuery.startAfter(it)
        } ?: baseQuery

        val querySnapshot = query.get().await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val newLastSnapshot = querySnapshot.documents.lastOrNull()

        Log.d("HomeEvents", "Fetched ${events.size} events")

        return Pair(events, newLastSnapshot)
    }


    suspend fun getHomeRecommendedEventsData(limit: Long): Pair<List<Map<String, Any>>, Set<String>> {

        Log.d("Filter", "Had to send a query to Firebase for Recommended Events")
        val userId = auth.currentUser?.uid ?: return Pair(emptyList(), emptySet())
        val querySnapshot = db.collection("users").document(userId)
            .get()
            .await()

        val recommendedEventsIds = querySnapshot.get("recommended_events") as? List<String> ?: emptyList()

        val events = mutableListOf<Map<String, Any>>()
        val shownEvents = mutableSetOf<String>()

        // Show the first 'limit' recommended events
        for (eventId in recommendedEventsIds.take(limit.toInt())) {
            if (shownEvents.contains(eventId)) continue
            shownEvents.add(eventId)
            val eventDocument = db.collection("events").document(eventId).get().await()
            if (eventDocument.exists()) { // Check if document exists before accessing data
                val eventData = eventDocument.data ?: emptyMap()
                val eventMapWithId = eventData.toMutableMap() // Convert to mutable map
                eventMapWithId["id"] = eventDocument.id // Add document ID to the map
                events.add(eventMapWithId)
            }
        }

        // Return the shown events along with the events fetched
        return Pair(events, shownEvents)
    }

    suspend fun getNextHomeRecommendedEventsData(
        limit: Long = 3,
        offsetIds: Set<String>
    ): List<Map<String, Any>> {

        Log.d("Filter", "Querying more recommended events -> End of Row")
        val events = mutableListOf<Map<String, Any>>()
        val userId = auth.currentUser?.uid ?: return emptyList()

        // Fetch user recommended events
        val userSnapshot = db.collection("users").document(userId)
            .get()
            .await()
        Log.d("User ID", userId)

        val recommendedEventsIds = userSnapshot.get("recommended_events") as? List<String> ?: emptyList()
        // Skip events that have already been shown based on the provided offsetIds
        val offsetIdsNotName = offsetIds.mapNotNull { name -> getEventIdByName(name) }.toSet()

        val newRecommendedIds = recommendedEventsIds.filterNot { offsetIdsNotName.contains(it) }
        Log.d("New Recommended Events", newRecommendedIds.toString())
        var count = 0
        for (eventId in newRecommendedIds) {
            if (count >= limit) break
            val eventDocument = db.collection("events").document(eventId).get().await()
            if (eventDocument.exists()) {
                val eventData = eventDocument.data ?: emptyMap()
                val eventMapWithId = eventData.toMutableMap()
                eventMapWithId["id"] = eventDocument.id
                events.add(eventMapWithId)
                count++
            }
        }

        return events
    }

    private suspend fun getEventIdByName(eventName: String): String? {
        val snapshot = db
            .collection("events")
            .whereEqualTo("name", eventName)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.id
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

    suspend fun getSearchEventsData(limit: Long): Pair<List<Map<String, Any>>, DocumentSnapshot?> {

        Log.d("SearchEvents", "Query for more events")
        val querySnapshot = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val lastSnapshot = querySnapshot.documents.lastOrNull()

        return Pair(events, lastSnapshot)
    }

    suspend fun getNextSearchEventsData(
        limit: Long = 5,
        lastDocumentSnapshot: DocumentSnapshot? = null
    ): Pair<List<Map<String, Any>>, DocumentSnapshot?> {
        Log.d("SearchEvents", "Querying next events with limit = $limit")

        val baseQuery = db.collection("events")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit)

        val query = lastDocumentSnapshot?.let {
            baseQuery.startAfter(it)
        } ?: baseQuery

        val querySnapshot = query.get().await()

        val events = querySnapshot.documents.map { doc ->
            val eventData = doc.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = doc.id
            eventMapWithId
        }

        val newLastSnapshot = querySnapshot.documents.lastOrNull()

        Log.d("SearchEvents", "Fetched ${events.size} events")

        return Pair(events, newLastSnapshot)
    }

    suspend fun getSkillsData(): List<Map<String, Any>> {
        val querySnapshot = db.collection("skills")
            .get()
            .await()

        val skills = mutableListOf<Map<String, Any>>()

        for (skillDocument in querySnapshot.documents) {
            skills.add(skillDocument.data ?: emptyMap())
        }

        return skills
    }

    suspend fun getLocationsData(): List<Map<String, Any>> {
        val querySnapshot = db.collection("locations")
            .get()
            .await()

        val locations = mutableListOf<Map<String, Any>>()

        for (locationDocument in querySnapshot.documents) {
            locations.add(locationDocument.data ?: emptyMap())
        }

        return locations
    }
}
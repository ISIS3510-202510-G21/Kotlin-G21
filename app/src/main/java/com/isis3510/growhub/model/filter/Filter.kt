package com.isis3510.growhub.model.filter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.LruCache
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.isis3510.growhub.utils.ProfileCache

/**
 * Created by: Juan Manuel Jáuregui
 */

class Filter(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val homeEventsCache: LruCache<String, List<Map<String, Any>>>
    private val searchEventsCache: LruCache<String, List<Map<String, Any>>>
    private val myEventsCache: LruCache<String, List<Map<String, Any>>>

    init {
        val maxCacheSize = 5 * 1024 * 1024
        homeEventsCache = LruCache(maxCacheSize)
        searchEventsCache = LruCache(maxCacheSize)
        myEventsCache = LruCache(maxCacheSize)
    }

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

        val cacheKey = "my_events_initial_${limit}"

        val cachedEvents = myEventsCache.get(cacheKey)
        if (cachedEvents != null) {
            Log.d("MyEvents", "Get ${cachedEvents.size} events from Cache")
            return Pair(cachedEvents, null)
        }

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

        searchEventsCache.put(cacheKey, filteredEvents)

        return Pair(filteredEvents, lastSnapshot)
    }

    suspend fun getNextMyEventsData(
        limit: Long = 5,
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

        val cacheKey = "my_events_initial_${limit}"

        val cachedEvents = myEventsCache.get(cacheKey)
        if (cachedEvents != null) {
            Log.d("MyEvents", "Get ${cachedEvents.size} events from Cache")
            return Pair(cachedEvents, null)
        }

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

        searchEventsCache.put(cacheKey, filteredEvents)

        return Pair(filteredEvents, lastSnapshot)
    }

    suspend fun getNextMyEventsCreateData(
        limit: Long = 5,
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

    suspend fun getHomeEventsData(limit: Long): List<Map<String, Any>> {
        val cacheKey = "home_events_initial_${limit}" // Key for the initial cache

        // 1. Verify if there are data in the initial cache
        val cachedEvents = homeEventsCache.get(cacheKey)
        if (cachedEvents != null) {
            Log.d("Filter", "Obtained ${cachedEvents.size} upcoming events from Cache")
            return cachedEvents
        }

        // 2. Consult Firebase if there are no cached data
        Log.d("Filter", "Had to send a query to Firebase for Upcoming Events")
        val querySnapshot = db.collection("events")
            .limit(limit) // Apply limit to the initial query for faster performance
            .get()
            .await()

        val events = mutableListOf<Map<String, Any>>()
        for (eventDocument in querySnapshot.documents) {
            val eventData = eventDocument.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = eventDocument.id
            events.add(eventMapWithId)
        }

        // 3. Store the initial data on cache memory
        homeEventsCache.put(cacheKey, events)
        return events
    }

    suspend fun getNextHomeEventsData(limit: Long = 3, excludedIds: List<String>): List<Map<String, Any>> {
        // Consult Firebase to fetch the next events for the Home after we get to the end
        Log.d("Filter", "Another query for more upcoming events -> End of Row, excluding IDs: $excludedIds")
        var query = db.collection("events")
            .whereNotIn("__name__", excludedIds)
            .limit(limit)
            .get()
            .await()

        val querySnapshot = query

        val events = mutableListOf<Map<String, Any>>()
        for (eventDocument in querySnapshot.documents) {
            val eventData = eventDocument.data ?: emptyMap()
            val eventMapWithId = eventData.toMutableMap()
            eventMapWithId["id"] = eventDocument.id
            events.add(eventMapWithId)
        }
        Log.d("Filter", "Found ${events.size} new upcoming events (after exclusion)")
        return events // Additional events are not cached to avoid memory bloating
    }


    suspend fun getHomeRecommendedEventsData(limit: Long): List<Map<String, Any>> {
        val cacheKey = "recommended_events_initial_${limit}" // Key for initial cache for recommended

        // 1. Verify initial cache
        val cachedEvents = homeEventsCache.get(cacheKey)
        if (cachedEvents != null) {
            Log.d("Filter", "Obtained ${cachedEvents.size} recommended events from Cache")
            return cachedEvents
        }

        // 2. Query Firebase for recommendations
        Log.d("Filter", "Had to send a query to Firebase for Recommended Events")
        val userId = auth.currentUser?.uid ?: return emptyList()
        val querySnapshotUsuario = db.collection("users").document(userId)
            .get()
            .await()
        val recommendedEventsIds = querySnapshotUsuario.get("recommended_events") as? List<String> ?: emptyList()

        val events = mutableListOf<Map<String, Any>>()
        var count = 0
        for (eventId in recommendedEventsIds) {
            if (count >= limit) break
            val eventDocument = db.collection("events").document(eventId).get().await()
            if (eventDocument.exists()){ // Check if document exists before accessing data
                val eventData = eventDocument.data ?: emptyMap()
                val eventMapWithId = eventData.toMutableMap() // Convert to mutable map
                eventMapWithId["id"] = eventDocument.id // Add document ID to the map
                events.add(eventMapWithId)
                count++
            }
        }

        // 3. Store initial recommendations on cache
        homeEventsCache.put(cacheKey, events)
        return events
    }


    suspend fun getNextHomeRecommendedEventsData(limit: Long = 3, offset: Long): List<Map<String, Any>> {
        // Bring the following recommended events
        Log.d("Filter", "Another query for more recommended events -> End of Row")
        val events = mutableListOf<Map<String, Any>>()
        var count = offset
        val userId = auth.currentUser?.uid ?: return emptyList()
        val querySnapshotUsuario = db.collection("users").document(userId)
            .get()
            .await()
        val recommendedEventsIds = querySnapshotUsuario.get("recommended_events") as? List<String> ?: emptyList()
        val newRecommendedIds = if (recommendedEventsIds.size > count) {recommendedEventsIds.subList(
            offset.toInt(), recommendedEventsIds.size)} else {emptyList()}
        for (eventId in newRecommendedIds) {
            if (count >= offset + limit ) break
            if (count >= offset) {
                val eventDocument = db.collection("events").document(eventId).get().await()
                if (eventDocument.exists()) { // Check if document exists before accessing data
                    val eventData = eventDocument.data ?: emptyMap()
                    val eventMapWithId = eventData.toMutableMap() // Convert to mutable map
                    eventMapWithId["id"] = eventDocument.id // Add document ID to the map
                    events.add(eventMapWithId)
                }
            }
            count++
        }
        return events // Additional recommendations are not cached to avoid bloating
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
        val cacheKey = "search_events_initial_${limit}"

        val cachedEvents = searchEventsCache.get(cacheKey)
        if (cachedEvents != null) {
            Log.d("SearchEvents", "Get ${cachedEvents.size} events from Cache")
            return Pair(cachedEvents, null)
        }

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

        searchEventsCache.put(cacheKey, events)

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
package com.isis3510.growhub.model.facade

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.isis3510.growhub.model.filter.Filter
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.Location
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.ZoneId

/**
 * Created by: Juan Manuel Jáuregui
 */

class FirebaseServicesFacade(private val filter: Filter = Filter()) {

    suspend fun fetchUserProfile(): Profile? = withContext(Dispatchers.IO){
        return@withContext try {
            val data = filter.getProfileData()

            Profile(
                profilePicture = data?.get("profilePicture") as? String ?: "",
                description = data?.get("description") as? String ?: "",
                interests = data?.get("interests") as? List<String> ?: emptyList(),
                followers = data?.get("followers") as? Int ?: 0,
                following = data?.get("following") as? Int ?: 0,
                name = data?.get("name") as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching user profile", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEvents(limit: Long = 25): Pair<List<Event>, DocumentSnapshot?> = withContext(Dispatchers.IO){
        return@withContext try {
            val (filteredEvents, lastSnapshot) = filter.getMyEventsData(limit)
            Pair(mapFilterEventsToEvents(filteredEvents), lastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching my events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextMyEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<Event>, DocumentSnapshot?> = withContext(Dispatchers.IO){
        return@withContext try {
            val (filteredEvents, newLastSnapshot) = filter.getNextMyEventsData(limit, lastSnapshot)
            Pair(mapFilterEventsToEvents(filteredEvents), newLastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next my events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEventsCreate(limit: Long = 25): Pair<List<Event>, DocumentSnapshot?> = withContext(Dispatchers.IO){
        return@withContext try {
            val (filteredEvents, lastSnapshot) = filter.getMyEventsCreateData(limit)
            Pair(mapFilterEventsToEvents(filteredEvents), lastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching my events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextMyEventsCreate(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<Event>, DocumentSnapshot?> = withContext(Dispatchers.IO){
        return@withContext try {
            val (filteredEvents, newLastSnapshot) = filter.getNextMyEventsCreateData(limit, lastSnapshot)
            Pair(mapFilterEventsToEvents(filteredEvents), newLastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next my events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeEvents(limit: Long = 5): Pair<List<Event>, DocumentSnapshot?> {
        return try {
            val (filteredEvents, lastSnapshot) = filter.getHomeEventsData(limit)
            Pair(mapFilterEventsToEvents(filteredEvents), lastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching home events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<Event>, DocumentSnapshot?> {
        return try {
            val (filteredEvents, newLastSnapshot) = filter.getNextHomeEventsData(limit, lastSnapshot)
            Pair(mapFilterEventsToEvents(filteredEvents), newLastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next home events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeRecommendedEvents(limit: Long = 5): Pair<List<Event>, Set<String>> {
        try {
            val (filteredEvents, offsetIds) = filter.getHomeRecommendedEventsData(limit)
            return Pair(mapFilterEventsToEvents(filteredEvents), offsetIds)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching recommended home events", e)
            return Pair(emptyList(), emptySet())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeRecommendedEvents(limit: Long = 3, offsetIds: Set<String>): List<Event> {
        try {
            val filteredEvents = filter.getNextHomeRecommendedEventsData(limit, offsetIds)
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next recommended home events", e)
            return emptyList()
        }
    }

    suspend fun fetchCategories(): List<Category> {
        return try {
            val filteredCategories = filter.getCategoriesData()
            val categories = mutableListOf<Category>()
            for (category in filteredCategories) {
                val name = category["name"] as? String ?: ""
                val categoryExtracted = Category(name = name)
                categories.add(categoryExtracted)
            }
            categories
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching categories", e)
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchSearchEvents(limit: Long = 5): Pair<List<Event>, DocumentSnapshot?> = withContext(Dispatchers.IO){
        return@withContext try {
            val (filteredEvents, lastSnapshot) = filter.getSearchEventsData(limit)
            Pair(mapFilterEventsToEvents(filteredEvents), lastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching search events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextSearchEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<Event>, DocumentSnapshot?> = withContext(Dispatchers.IO){
        return@withContext try {
            val (filteredEvents, newLastSnapshot) = filter.getNextSearchEventsData(limit, lastSnapshot)
            Pair(mapFilterEventsToEvents(filteredEvents), newLastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next search events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun mapFilterEventsToEvents(filteredEvents: List<Map<String, Any>>): List<Event> = withContext(Dispatchers.Default) {
        val events = mutableListOf<Event>()

        for (event in filteredEvents) {

            val name = event["name"] as? String ?: ""
            val imageUrl = event["image"] as? String ?: ""
            val description = event["description"] as? String ?: ""
            val cost = (event["cost"] as? Number)?.toInt() ?: 0
            val attendees = (event["attendees"] as? List<DocumentReference>) ?: emptyList()
            val eventId = event["id"] as? String ?: ""

            // Extract the creators name from the users collection
            val creatorRef = event["creator_id"] as? DocumentReference
            val creatorDoc = creatorRef?.get()?.await()
            val creatorName = creatorDoc?.getString("name") ?: ""

            // Extract the category name from the categories collection
            val categoryRef = event["category"] as? DocumentReference
            val categoryName = if (categoryRef != null) {
                val categoryDoc = categoryRef.get().await()
                categoryDoc.getString("name") ?: ""
            } else {
                ""
            }

            // Extract the location data from the locations collection
            val locationRef = event["location_id"] as? DocumentReference
            var locationName = ""
            var locationCity = ""
            var locationLatitude = 0.0
            var locationLongitude = 0.0
            if (locationRef != null) {
                val locationDoc = locationRef.get().await()
                locationName = locationDoc.getString("address") ?: ""
                locationCity = locationDoc.getString("city") ?: ""
                locationLatitude = locationDoc.getDouble("latitude") ?: 0.0
                locationLongitude = locationDoc.getDouble("longitude") ?: 0.0
            }

            // Extract the attendees name from the users collection
            val attendeesNames = attendees.mapNotNull { attendeeRef ->
                val attendeeDoc = attendeeRef.get().await()
                attendeeDoc.getString("name")
            }

            // Extract the skills from the skills collection
            val skillsRef = (event["skills"] as? List<DocumentReference>) ?: emptyList()
            val skills = skillsRef.mapNotNull { skillReference ->
                val skillDoc = skillReference.get().await()
                skillDoc.getString("name")
            }

            // Extract the start date and end date from the events collection
            val startTimestamp = event["start_date"] as? Timestamp
            val endTimestamp = event["end_date"] as? Timestamp

            // Convert Firestore Timestamp to LocalDateTime
            val startDateTime =
                startTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())
                    ?.toLocalDateTime()
            val endDateTime =
                endTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())
                    ?.toLocalDateTime()

            // Convert date into "dd/MM/yyyy" format
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val formattedStartDate = startDateTime?.format(dateFormatter)
            val formattedEndDate = endDateTime?.format(dateFormatter)

            val locationExtracted = Location(
                address = locationName,
                city = locationCity,
                latitude = locationLatitude,
                longitude = locationLongitude
            )

            val eventExtracted = Event(
                name = name,
                imageUrl = imageUrl,
                description = description,
                cost = cost,
                attendees = attendeesNames,
                startDate = formattedStartDate.toString(),
                endDate = formattedEndDate.toString(),
                category = categoryName,
                location = locationExtracted,
                skills = skills,
                creator = creatorName
            )
            events.add(eventExtracted)
        }
        return@withContext events
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchRegistrationData(eventID: String): Event {
        try {
            val filteredData = filter.getRegistrationData(eventID)

            // Extract the event name from the events collection
            val eventName = filteredData["name"] as? String ?: ""

            // Extract the creators name from the users collection
            val creatorRef = filteredData["creator_id"] as? DocumentReference
            val creatorDoc = creatorRef?.get()?.await()
            val creatorName = creatorDoc?.getString("name") ?: ""

            // Extract the attendees as a list of strings
            val attendees = (filteredData["attendees"] as? List<DocumentReference>) ?: emptyList()
            val attendeesNames = attendees.mapNotNull { attendeeRef ->
                val attendeeDoc = attendeeRef.get().await()
                attendeeDoc.getString("name")
            }

            // Convert Firestore Timestamp to LocalDateTime
            val startTimestamp = filteredData["start_date"] as? Timestamp
            val endTimestamp = filteredData["end_date"] as? Timestamp
            val startDateTime =
                startTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())
                    ?.toLocalDateTime()
            val endDateTime =
                endTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())

            // Extract the category name from the categories collection
            val categoryRef = filteredData["category"] as? DocumentReference
            val categoryName = if (categoryRef != null) {
                val categoryDoc = categoryRef.get().await()
                categoryDoc.getString("name") ?: ""
            } else {
                ""
            }

            // Extract the skills from the skills collection
            val skillsRef = (filteredData["skills"] as? List<DocumentReference>) ?: emptyList()
            val skills = skillsRef.mapNotNull { skillReference ->
                val skillDoc = skillReference.get().await()
                skillDoc.getString("name")
            }

            // Extract the location address from the locations collection
            val locationRef = filteredData["location_id"] as? DocumentReference
            val locationDoc = locationRef?.get()?.await()
            val locationName = locationDoc?.getString("address") ?: ""
            val locationCity = locationDoc?.getString("city") ?: ""
            val locationLatitude = locationDoc?.getDouble("latitude") ?: 0.0
            val locationLongitude = locationDoc?.getDouble("longitude") ?: 0.0

            return Event(
                name = eventName,
                description = filteredData["description"] as? String ?: "",
                location = Location(address = locationName, city = locationCity, latitude = locationLatitude, longitude = locationLongitude),
                startDate = startDateTime.toString(),
                endDate = endDateTime.toString(),
                category = categoryName,
                imageUrl = filteredData["image"] as? String ?: "",
                cost = (filteredData["cost"] as? Number)?.toInt() ?: 0,
                attendees = attendeesNames,
                skills = skills,
                creator = creatorName
            )

        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching registration data", e)
            throw e
        }
    }

    suspend fun fetchSkills(): List<String> = withContext(Dispatchers.IO) {
        val skills = filter.getSkillsData()
        val skillsList = mutableListOf<String>()
        for (skill in skills) {
            val name = skill["name"] as? String ?: ""
            skillsList.add(name)
        }
        return@withContext skillsList
    }

    suspend fun fetchLocations(): List<String> = withContext(Dispatchers.IO) {
        val locations = filter.getLocationsData()
        val locationsList = mutableListOf<String>()
        for (location in locations) {
            val city = location["city"] as? String ?: ""
            if (!locationsList.contains(city)) {
                locationsList.add(city)
            }
        }
        return@withContext locationsList
    }
}

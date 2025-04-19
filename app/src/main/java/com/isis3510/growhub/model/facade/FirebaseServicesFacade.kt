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
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.tasks.await
import java.time.ZoneId

/**
 * Created by: Juan Manuel Jáuregui
 */

class FirebaseServicesFacade(
    private val filter: Filter = Filter(),
) {
    suspend fun fetchUserProfile(): Profile? {
        try {
            val filteredData = filter.getProfileData()

            val interests = filteredData?.get("interests") as? List<DocumentReference>
            val followers = filteredData?.get("followers") as? List<DocumentReference>
            val following = filteredData?.get("following") as? List<DocumentReference>
            val userRef = filteredData?.get("user_ref") as? DocumentReference

            // Extract interests names
            val interestsNames = interests?.mapNotNull { interestRef ->
                val interestDoc = interestRef.get().await()
                interestDoc.getString("name")
            } ?: emptyList()

            // Extract followers count
            val followersCount = followers?.size ?: 0

            // Extract following count
            val followingCount = following?.size ?: 0

            // Extract user name from users collection
            val userDoc = userRef?.get()?.await()
            val userName = userDoc?.getString("name") ?: ""

            return Profile(
                profilePicture = filteredData?.get("profilePicture") as? String ?: "",
                description = filteredData?.get("description") as? String ?: "",
                interests = interestsNames,
                followers = followersCount,
                following = followingCount,
                name = userName
            )
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching user profile", e)
            return null

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEvents(): List<Event> {
        try {
            val filteredEvents = filter.getEventsData()
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching my events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeEvents(limit: Long = 5): List<Event> {
        try {
            val filteredEvents = filter.getHomeEventsData(limit)
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching home events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeEvents(limit: Long = 3, excludedIds: List<String>): List<Event> {
        try {
            val filteredEvents = filter.getNextHomeEventsData(limit, excludedIds)
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next home events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeRecommendedEvents(limit: Long = 5): List<Event> {
        try {
            val filteredEvents = filter.getHomeRecommendedEventsData(limit)
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching recommended home events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeRecommendedEvents(limit: Long = 3, offset: Long): List<Event> {
        try {
            val filteredEvents = filter.getNextHomeRecommendedEventsData(limit, offset)
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next recommended home events", e)
            return emptyList()
        }
    }


    suspend fun fetchCategories(): List<Category> {
        try {
            val filteredCategories = filter.getCategoriesData()

            val categories = mutableListOf<Category>()

            for (category in filteredCategories) {
                val name = category["name"] as? String ?: ""
                val categoryExtracted = Category(name = name)
                categories.add(categoryExtracted)
            }

            return categories
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching categories", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchSearchEvents(limit: Long = 5): List<Event> {
        try {
            val filteredEvents = filter.getSearchEventsData(limit)
            return mapFilterEventsToEvents(filteredEvents)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching search events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextSearchEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<Event>, DocumentSnapshot?> {
        return try {
            val (filteredEvents, newLastSnapshot) = filter.getNextSearchEventsData(limit, lastSnapshot)
            Pair(mapFilterEventsToEvents(filteredEvents), newLastSnapshot)
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching next search events", e)
            Pair(emptyList(), null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun mapFilterEventsToEvents(filteredEvents: List<Map<String, Any>>): List<Event> {
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

            // Extract the location address from the locations collection
            val locationRef = event["location_id"] as? DocumentReference
            val locationName = if (locationRef != null) {
                val locationDoc = locationRef.get().await()
                locationDoc.getString("address") ?: ""
            } else {
                ""
            }

            // Extract city from the locations collection
            val locationCity = if (locationRef != null) {
                val locationDoc = locationRef.get().await()
                locationDoc.getString("city") ?: ""
            } else {
                ""
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

            val eventExtracted = Event(
                id = eventId,
                name = name,
                imageUrl = imageUrl,
                description = description,
                cost = cost,
                attendees = attendeesNames,
                startDate = formattedStartDate.toString(),
                endDate = formattedEndDate.toString(),
                category = categoryName,
                location = locationCity,
                skills = skills,
                creator = creatorName
            )
            events.add(eventExtracted)
        }
        return events
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
            val locationDetails = locationDoc?.getString("details") ?: ""

            // Mix the location name and details as a single string
            val locationString = "$locationName, $locationDetails"

            return Event(
                id = eventID,
                name = eventName,
                description = filteredData["description"] as? String ?: "",
                location = locationString,
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

    suspend fun fetchSkills(): List<String> {
        val skills = filter.getSkillsData()
        val skillsList = mutableListOf<String>()
        for (skill in skills) {
            val name = skill["name"] as? String ?: ""
            skillsList.add(name)
        }
        return skillsList
    }

    suspend fun fetchLocations(): List<String> {
        val locations = filter.getLocationsData()
        val locationsList = mutableListOf<String>()
        for (location in locations) {
            val city = location["city"] as? String ?: ""
            if (!locationsList.contains(city)) {
                locationsList.add(city)
            }
        }
        return locationsList
    }
}
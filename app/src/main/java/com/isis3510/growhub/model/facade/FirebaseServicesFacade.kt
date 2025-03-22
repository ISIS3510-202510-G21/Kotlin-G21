package com.isis3510.growhub.model.facade

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
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

            val events = mutableListOf<Event>()

            for (event in filteredEvents) {

                val name = event["name"] as? String ?: ""
                val imageUrl = event["image"] as? String ?: ""
                val description = event["description"] as? String ?: ""
                val cost = (event["cost"] as? Number)?.toInt() ?: 0
                val attendees = (event["attendees"] as? List<DocumentReference>) ?: emptyList()

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
                    name = name,
                    imageUrl = imageUrl,
                    description = description,
                    cost = cost,
                    attendees = attendeesNames,
                    startDate = formattedStartDate.toString(),
                    endDate = formattedEndDate.toString(),
                    category = categoryName,
                    location = locationName,
                    city = locationCity
                )

                events.add(eventExtracted)
            }

            return events
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching my events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeEvents(): List<Event> {
        try {
            val filteredEvents = filter.getHomeEventsData()

            val events = mutableListOf<Event>()

            for (event in filteredEvents) {

                val name = event["name"] as? String ?: ""
                val imageUrl = event["image"] as? String ?: ""
                val description = event["description"] as? String ?: ""
                val cost = (event["cost"] as? Number)?.toInt() ?: 0
                val attendees = (event["attendees"] as? List<DocumentReference>) ?: emptyList()

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
                    name = name,
                    imageUrl = imageUrl,
                    description = description,
                    cost = cost,
                    attendees = attendeesNames,
                    startDate = formattedStartDate.toString(),
                    endDate = formattedEndDate.toString(),
                    category = categoryName,
                    location = locationName,
                    city = locationCity
                )

                events.add(eventExtracted)
            }

            return events
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching home events", e)
            return emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeRecommendedEvents(): List<Event> {
        try {
            val filteredEvents = filter.getHomeRecommendedEventsData()

            val events = mutableListOf<Event>()

            for (event in filteredEvents) {

                val name = event["name"] as? String ?: ""
                val imageUrl = event["image"] as? String ?: ""
                val description = event["description"] as? String ?: ""
                val cost = (event["cost"] as? Number)?.toInt() ?: 0
                val attendees = (event["attendees"] as? List<DocumentReference>) ?: emptyList()

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
                    name = name,
                    imageUrl = imageUrl,
                    description = description,
                    cost = cost,
                    attendees = attendeesNames,
                    startDate = formattedStartDate.toString(),
                    endDate = formattedEndDate.toString(),
                    category = categoryName,
                    location = locationName,
                    city = locationCity
                )

                events.add(eventExtracted)
            }

            return events
        } catch (e: Exception) {
            Log.e("FirebaseServicesFacade", "Error fetching home events", e)
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
}
package com.isis3510.growhub.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel : ViewModel() {
    val upcomingEvents = mutableStateListOf<Event>()
    val nearbyEvents = mutableStateListOf<Event>()
    val recommendedEvents = mutableStateListOf<Event>()
    val categories = mutableStateListOf<Category>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadUpcomingEventsFromFirebase()
        loadEventsFromFirebase()
        loadCategoriesFromFirebase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUpcomingEventsFromFirebase() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            val querySnapshot = db.collection("events")
                .get()
                .await()

            val attendedEvents = mutableListOf<DocumentReference>()

            for (eventDocument in querySnapshot.documents) {
                val attendees = eventDocument.data?.get("attendees") as? List<DocumentReference>

                // Check if the user is in the attendees list
                if (attendees?.any { it.id == userId } == true) {
                    val eventRef = eventDocument.reference
                    attendedEvents.add(eventRef)
                }
            }

            for (event in attendedEvents) {
                val eventDocument = event.get().await()
                val eventData = eventDocument.data ?: emptyMap()

                val name = eventData["name"] as? String ?: ""
                val imageUrl = eventData["image"] as? String ?: ""
                val description = eventData["description"] as? String ?: ""
                val cost = (eventData["cost"] as? Number)?.toInt() ?: 0
                val attendees = (eventData["attendees"] as? List<DocumentReference>) ?: emptyList()

                // Extract the category name from the categories collection
                val categoryRef = eventData["category"] as? DocumentReference
                val categoryName = if (categoryRef != null) {
                    val categoryDoc = categoryRef.get().await()
                    categoryDoc.getString("name") ?: ""
                } else {
                    ""
                }

                // Extract the location address from the locations collection
                val locationRef = eventData["location_id"] as? DocumentReference
                val locationName = if (locationRef != null) {
                    val locationDoc = locationRef.get().await()
                    locationDoc.getString("address") ?: ""
                } else {
                    ""
                }

                // Extract the attendees name from the users collection
                val attendeesNames = attendees.mapNotNull { attendeeRef ->
                    val attendeeDoc = attendeeRef.get().await()
                    attendeeDoc.getString("name")
                }

                // Extract the start date and end date from the events collection
                val startTimestamp = eventData["start_date"] as? Timestamp
                val endTimestamp = eventData["end_date"] as? Timestamp

                // Convert Firestore Timestamp to LocalDateTime
                val startDateTime = startTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                val endDateTime = endTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

                // Convert date into "dd/MM/yyyy" format
                val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formattedStartDate = startDateTime?.format(dateFormatter)
                val formattedEndDate = endDateTime?.format(dateFormatter)

                // Get today's date in LocalDateTime format
                val todayDateTime = LocalDateTime.now()

                val eventExtracted = Event(
                    name = name,
                    imageUrl = imageUrl,
                    description = description,
                    cost = cost,
                    attendees = attendeesNames,
                    startDate = formattedStartDate.toString(),
                    endDate = formattedEndDate.toString(),
                    category = categoryName,
                    location = locationName
                )

                // Add the event to the appropriate list based on the start date
                if (startDateTime != null && startDateTime.isAfter(todayDateTime)) {
                    upcomingEvents.add(eventExtracted)
                }
            }
        }
    }

    private fun loadEventsFromFirebase() {
        viewModelScope.launch {
            val querySnapshot = db.collection("events")
                .get()
                .await()

            for (eventDocument in querySnapshot.documents) {
                val eventData = eventDocument.data ?: emptyMap()

                val name = eventData["name"] as? String ?: ""
                val imageUrl = eventData["image"] as? String ?: ""
                val description = eventData["description"] as? String ?: ""
                val cost = (eventData["cost"] as? Number)?.toInt() ?: 0
                val attendees = (eventData["attendees"] as? List<DocumentReference>) ?: emptyList()

                // Extract the category name from the categories collection
                val categoryRef = eventData["category"] as? DocumentReference
                val categoryName = if (categoryRef != null) {
                    val categoryDoc = categoryRef.get().await()
                    categoryDoc.getString("name") ?: ""
                } else {
                    ""
                }

                // Extract the location address from the locations collection
                val locationRef = eventData["location_id"] as? DocumentReference
                val locationName = if (locationRef != null) {
                    val locationDoc = locationRef.get().await()
                    locationDoc.getString("address") ?: ""
                } else {
                    ""
                }

                // Extract the attendees name from the users collection
                val attendeesNames = attendees.mapNotNull { attendeeRef ->
                    val attendeeDoc = attendeeRef.get().await()
                    attendeeDoc.getString("name")
                }

                // Extract the start date and end date from the events collection
                val startTimestamp = eventData["start_date"] as? Timestamp
                val endTimestamp = eventData["end_date"] as? Timestamp

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
                    location = locationName
                )

                // Add only the first 5 events to the nearbyEvents list
                if (nearbyEvents.size < 5) {
                    nearbyEvents.add(eventExtracted)
                }

                // Add only the first 5 events to the recommendedEvents list
                if (recommendedEvents.size < 5) {
                    recommendedEvents.add(eventExtracted)
                }
            }
        }
    }

    private fun loadCategoriesFromFirebase() {
        viewModelScope.launch {
            val querySnapshot = db.collection("categories")
                .get()
                .await()

            for (categoryDocument in querySnapshot.documents) {
                val categoryData = categoryDocument.data ?: emptyMap()
                val name = categoryData["name"] as? String

                if (name != null) {
                    val categoryExtracted = Category(name = name)
                    categories.add(categoryExtracted)
                }
            }
        }
    }
}

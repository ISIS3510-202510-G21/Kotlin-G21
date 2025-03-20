package com.isis3510.growhub.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
class MyEventsViewModel : ViewModel() {

    val upcomingEvents = mutableStateListOf<Event>()
    val previousEvents = mutableStateListOf<Event>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadEventsFromFirebase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadEventsFromFirebase() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            val userDocRef = db.collection("users").document(userId)

            val querySnapshot = db.collection("events")
                .whereEqualTo("user_ref", userDocRef)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val eventData = document.data ?: emptyMap()

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

                // Extract the location name from the locations collection
                val locationRef = eventData["location"] as? DocumentReference
                val locationName = if (locationRef != null) {
                    val locationDoc = locationRef.get().await()
                    locationDoc.getString("name") ?: ""
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

                // Get today's date in LocalDateTime format
                val todayDateTime = LocalDateTime.now()

                val eventExtracted = Event(
                    name = name,
                    imageUrl = imageUrl,
                    description = description,
                    cost = cost,
                    attendees = attendeesNames,
                    startDate = startDateTime.toString(),
                    endDate = endDateTime.toString(),
                    category = categoryName,
                    location = locationName
                )

                // Add the event to the appropriate list based on the start date
                if (startDateTime != null && startDateTime.isAfter(todayDateTime)) {
                    upcomingEvents.add(eventExtracted)
                } else {
                    previousEvents.add(eventExtracted)
                }
            }

        }
    }
}
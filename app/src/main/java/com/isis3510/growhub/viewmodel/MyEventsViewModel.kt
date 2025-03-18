package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.R
import kotlinx.coroutines.launch

// Event Data Class (meanwhile)
data class Event(
    val id: String = "",
    val imageRes: Int = 0,
    val date: String = "",
    val title: String = "",
    var isPaid: Boolean = true
)

// My Events View Model
class MyEventsViewModel : ViewModel() {

    // Events list
    val upcomingEvents = mutableStateListOf<Event>()
    val previousEvents = mutableStateListOf<Event>()

    // Connection to Firebase
    //private val auth = FirebaseAuth.getInstance()
    //private val db = FirebaseFirestore.getInstance()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val mockData = listOf(
                Event("1", R.drawable.event1, "Wed, Apr 28 - 5:30 PM", "A Virtual Evening of Smooth Jazz", true),
                Event("2", R.drawable.event2, "Wed, Apr 28 - 5:30 PM", "A Virtual Evening of Smooth Jazz", false),
                Event("3", R.drawable.event3, "Thu, Mar 6 - 1:30 PM", "International Gala Music Festival", true),
                Event("4", R.drawable.event4, "Wed, Feb 25 - 3:30 PM", "Women Leadership Conference", true),
            )
            upcomingEvents.addAll(mockData.take(2))
            previousEvents.addAll(mockData.takeLast(2))
        }
    }

    /*    private fun loadEvents() {
            val user = auth.currentUser

            viewModelScope.launch {
                try {
                    val snapshot = db.collection("events")
                        .whereArrayContains("attendees", user.uid) // Fetch events where user is an attendee
                        .get()
                        .await()

                    val eventList = snapshot.documents.mapNotNull { doc ->
                        val startDateString = doc.getString("start_date") ?: return@mapNotNull null
                        val startDate = parseDate(startDateString)

                        Event(
                            id = doc.id,
                            imageUrl = doc.getString("image") ?: "",
                            startDate = startDate,
                            title = doc.getString("name") ?: "Untitled Event",
                            isPaid = (doc.getDouble("cost") ?: 0.0) > 0
                        )
                    }

                    val now = Date()
                    upcomingEvents.clear()
                    previousEvents.clear()

                    for (event in eventList) {
                        if (event.startDate != null && event.startDate.after(now)) {
                            upcomingEvents.add(event)
                        } else {
                            previousEvents.add(event)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace() // Handle errors
                }
            }
        }

        // Helper function to parse Firestore date string
        private fun parseDate(dateString: String): Date? {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                format.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }*/
}
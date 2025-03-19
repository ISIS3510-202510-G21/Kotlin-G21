package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.isis3510.growhub.model.objects.Event

/**
 * Created by: Juan Manuel Jáuregui
 */

class MyEventsViewModel : ViewModel() {

    val upcomingEvents = mutableStateListOf<Event>()
    val previousEvents = mutableStateListOf<Event>()

    //private val auth = FirebaseAuth.getInstance()
    //private val db = FirebaseFirestore.getInstance()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val mockData = listOf(
                Event("1", "El Riqué (México) 5to Cir...", "Bogotá, Colombia", "February 26, 2025", "Music", "mock_image", 100.0),
                Event("2", "IEEE Zona Centro", "Bogotá, Colombia", "March 1, 2025", "Technology", "mock_image", 0.0),
                Event("3", "Taller Entrevista", "Bogotá, Colombia", "March 4, 2025", "Business", "mock_image", 10.0),
                Event("4", "XXIV Jornadas C...", "Bogotá, Colombia", "February 25, 2025", "Science", "mock_image", 50.0)
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
                        .whereArrayContains("attendees", user.uid)
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

        private fun parseDate(dateString: String): Date? {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                format.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }*/
}
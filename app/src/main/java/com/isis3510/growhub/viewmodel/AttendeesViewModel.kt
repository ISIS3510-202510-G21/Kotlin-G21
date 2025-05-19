package com.isis3510.growhub.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.Location
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Created by: Juan Manuel Jáuregui
 */

class AttendeesViewModel : ViewModel() {
    val event = mutableStateOf<Event?>(null)
    val loading = mutableStateOf(true)
    val attendeeProfiles = mutableStateOf<List<Profile>>(emptyList())

    private val db = FirebaseFirestore.getInstance()

    fun loadEvent(name: String) {
        if (!loading.value) loading.value = true

        viewModelScope.launch {

            val doc = withContext(Dispatchers.IO) {
                db.collection("events")
                    .whereEqualTo("name", name)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
            } ?: run {
                loading.value = false
                return@launch
            }

            val fullEvent = withContext(Dispatchers.IO) {
                coroutineScope {
                    suspend fun DocumentReference?.string(field: String): String =
                        this?.get()?.await()?.getString(field) ?: "Unknown"

                    val categoryD = async { doc.getDocumentReference("category").string("name") }
                    val locationD = async {
                        val locRef = doc.getDocumentReference("location_id")
                        val locSnap = locRef?.get()?.await()
                        if (locSnap != null && locSnap.exists()) {
                            Location(
                                address = locSnap.getString("address") ?: "Unknown",
                                city = locSnap.getString("city") ?: "Unknown",
                                latitude = locSnap.getDouble("latitude") ?: 0.0,
                                longitude = locSnap.getDouble("longitude") ?: 0.0,
                                university = locSnap.getBoolean("university") ?: false
                            )
                        } else Location("Unknown", "Unknown", "Unknown", 0.0, 0.0, false)
                    }
                    val attendeesD = async {
                        (doc["attendees"] as? List<*>)?.mapNotNull {
                            (it as? DocumentReference)?.string("name")
                        } ?: emptyList()
                    }
                    val skillsD = async {
                        (doc["skills"] as? List<*>)?.mapNotNull {
                            (it as? DocumentReference)?.string("name")
                        } ?: emptyList()
                    }
                    val creatorD = async {
                        val creatorRef = doc.getDocumentReference("creator_id")
                        val creatorSnap = creatorRef?.get()?.await()
                        creatorSnap?.getString("name") ?: "Unknown"
                    }

                    Event(
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        location = locationD.await(),
                        startDate = doc.getTimestamp("start_date")?.toDate()?.toString() ?: "",
                        endDate = doc.getTimestamp("end_date")?.toDate()?.toString() ?: "",
                        category = categoryD.await(),
                        imageUrl = doc.getString("image") ?: "",
                        cost = doc.getDouble("cost")?.toInt() ?: 0,
                        attendees = attendeesD.await(),
                        skills = skillsD.await(),
                        creator = creatorD.await(),
                    )
                }
            }

            event.value = fullEvent
            Log.d("AttendeesViewModel", "Event: $fullEvent")
            loading.value = false
        }
    }

    fun loadAttendees(event: Event) {
        if (!loading.value) loading.value = true

        viewModelScope.launch {
            val attendees = withContext(Dispatchers.IO) {
                event.attendees.mapNotNull { attendeeName ->
                    val attendeeDoc = db.collection("users")
                        .whereEqualTo("name", attendeeName)
                        .limit(1)

                    val attendeeSnap = attendeeDoc.get().await()
                    val attendeeData = attendeeSnap.documents.firstOrNull()
                    val attendeeRef = attendeeData?.reference
                    val attendeeId = attendeeRef?.id
                    Log.d("AttendeesViewModel", "Attendee ID: $attendeeId")

                    val attendeeProfileDoc = db.collection("profiles")
                        .whereEqualTo("user_ref",
                            attendeeId?.let { db.collection("users").document(it) })

                    val attendeeProfileSnap = attendeeProfileDoc.get().await()
                    val attendeeProfileData = attendeeProfileSnap.documents.firstOrNull()?.data
                    Log.d("AttendeesViewModel", "Attendee Profile Data: $attendeeProfileData")
                    if (attendeeProfileData != null) {
                        Profile(
                            name = attendeeName,
                            following = (attendeeProfileData["following"] as? Long)?.toInt() ?: 0,
                            followers = (attendeeProfileData["followers"] as? Long)?.toInt() ?: 0,
                            description = attendeeProfileData["description"].toString(),
                            headline = attendeeProfileData["headline"].toString(),
                            interests = (attendeeProfileData["interests"] as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
                            profilePicture = attendeeProfileData["profile_picture"].toString()
                        )
                    } else {
                        null
                    }
                }
            }
            attendeeProfiles.value = attendees
            loading.value = false
        }
    }

}
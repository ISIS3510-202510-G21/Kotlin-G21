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
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val _mostCommonHeadline = MutableStateFlow<String>("")
    val mostCommonHeadline: StateFlow<String> = _mostCommonHeadline

    private val _mostCommonInterest = MutableStateFlow<String>("")
    val mostCommonInterest: StateFlow<String> = _mostCommonInterest


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
                coroutineScope {
                    event.attendees.map { attendeeName ->
                        async<Profile?> {
                            try {
                                val attendeeSnap = db.collection("users")
                                    .whereEqualTo("name", attendeeName)
                                    .limit(1)
                                    .get()
                                    .await()

                                val attendeeData = attendeeSnap.documents.firstOrNull()
                                val attendeeRef = attendeeData?.reference
                                val attendeeId = attendeeRef?.id

                                val profileSnap = db.collection("profiles")
                                    .whereEqualTo("user_ref", attendeeId?.let { db.collection("users").document(it) })
                                    .get()
                                    .await()

                                val profileData = profileSnap.documents.firstOrNull()?.data ?: return@async null

                                val interestRefs = profileData["interests"] as? List<DocumentReference> ?: emptyList()
                                val interests = interestRefs.mapNotNull { it.get().await().getString("name") }

                                Profile(
                                    name = attendeeName,
                                    following = (profileData["following"] as? Long)?.toInt() ?: 0,
                                    followers = (profileData["followers"] as? Long)?.toInt() ?: 0,
                                    description = profileData["description"].toString(),
                                    headline = profileData["headline"].toString(),
                                    interests = interests,
                                    profilePicture = profileData["profile_picture"].toString()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
            }

            attendeeProfiles.value = attendees

            val commonHeadline = attendees
                .map { it.headline }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key.orEmpty()
            _mostCommonHeadline.value = commonHeadline

            val commonInterest = attendees
                .flatMap { it.interests }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
            _mostCommonInterest.value = commonInterest.maxByOrNull { it.value }?.key.orEmpty()

            loading.value = false
        }
    }

}
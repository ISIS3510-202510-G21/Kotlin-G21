package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.Repository.ProfileRepository
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.Location
import com.isis3510.growhub.model.objects.Profile
import com.isis3510.growhub.utils.AttendeeStatsCache
import com.isis3510.growhub.utils.ConnectionStatus
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

class AttendeesViewModel(application: Application) : AndroidViewModel(application) {
    val event = mutableStateOf<Event?>(null)
    val loading = mutableStateOf(true)
    val attendeeProfiles = mutableStateOf<List<Profile>>(emptyList())
    private val _mostCommonHeadline = MutableStateFlow<String>("")
    val mostCommonHeadline: StateFlow<String> = _mostCommonHeadline

    private val _mostCommonInterest = MutableStateFlow<String>("")
    val mostCommonInterest: StateFlow<String> = _mostCommonInterest

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val appLocalDatabase = AppLocalDatabase.getDatabase(application)
    private val eventRepository = EventRepository(appLocalDatabase)
    private val profileRepository = ProfileRepository(appLocalDatabase)
    private val connectivityViewModel = ConnectivityViewModel(application)

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
            eventRepository.storeEvents(listOf(fullEvent))
            eventRepository.deleteDuplicates()
            if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
                val localEvent = eventRepository.getEventByName(name)
                event.value = localEvent
            }
            loading.value = false
        }
    }

    fun loadAttendees(event: Event) {
        if (!loading.value) loading.value = true

        viewModelScope.launch {
            val cachedHeadline = AttendeeStatsCache.getHeadline(event.name)
            val cachedInterest = AttendeeStatsCache.getInterest(event.name)

            if (cachedHeadline != null && cachedInterest != null) {
                _mostCommonHeadline.value = cachedHeadline
                _mostCommonInterest.value = cachedInterest

                val cachedProfiles = profileRepository.getProfilesByName(event.attendees)
                attendeeProfiles.value = cachedProfiles
                loading.value = false
                return@launch
            }
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
            profileRepository.storeProfiles(attendees)
            profileRepository.deleteDuplicates()
            if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
                val localAttendees = profileRepository.getProfilesByName(event.attendees)
                attendeeProfiles.value = localAttendees
            }

            val commonHeadline = attendees
                .map { it.headline }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key.orEmpty()
            _mostCommonHeadline.value = commonHeadline
            AttendeeStatsCache.putHeadline(event.name, commonHeadline)

            val commonInterest = attendees
                .flatMap { it.interests }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key.orEmpty()
            _mostCommonInterest.value = commonInterest
            AttendeeStatsCache.putInterest(event.name, commonInterest)

            loading.value = false
        }
    }

}
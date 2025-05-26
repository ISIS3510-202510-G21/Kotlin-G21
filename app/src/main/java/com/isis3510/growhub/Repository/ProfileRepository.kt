package com.isis3510.growhub.Repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Profile
import com.isis3510.growhub.model.objects.toEntity
import com.isis3510.growhub.model.objects.toModel

class ProfileRepository(
    db: AppLocalDatabase
) {
    private val profileDao = db.profileDao()

    suspend fun getProfile(): Profile? {
        val entity = profileDao.getProfile()
        return entity?.toModel()
    }

    suspend fun getProfilesByName(names: List<String>): List<Profile> {
        val entities = profileDao.getProfilesByName(names)
        return entities.map { it.toModel() }
    }

    suspend fun storeProfile(profile: Profile) {
        val entity = profile.toEntity()
        profileDao.insertProfile(entity)
    }

    suspend fun storeProfiles(profiles: List<Profile>) {
        val entities = profiles.map { it.toEntity() }
        profileDao.insertProfiles(entities)
    }

    suspend fun deleteDuplicates() {
        profileDao.deleteDuplicates()
    }

    suspend fun saveEventStats(eventName: String, numberOfAttendees: Int, commonHeadline: String, commonInterest: String) {
        val stats = mapOf(
            "event_name" to eventName,
            "timestamp" to FieldValue.serverTimestamp(),
            "number_of_attendees" to numberOfAttendees,
            "most_common_headline" to commonHeadline,
            "most_common_interest" to commonInterest
        )

        try {
            FirebaseFirestore.getInstance()
                .collection("event_stats")
                .add(stats)
        } catch (e: Exception) {
            Log.e("Firestore", "Error saving event stats", e)
        }
    }

}
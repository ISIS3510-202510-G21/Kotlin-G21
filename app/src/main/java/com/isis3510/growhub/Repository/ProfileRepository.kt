package com.isis3510.growhub.Repository

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
}
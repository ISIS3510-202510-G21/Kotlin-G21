package com.isis3510.growhub.Repository

import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Profile
import com.isis3510.growhub.model.objects.toModel
import com.isis3510.growhub.model.objects.toEntity

class ProfileRepository(
    db: AppLocalDatabase
) {
    private val profileDao = db.profileDao()

    suspend fun getProfile(): Profile? {
        val entity = profileDao.getProfile()
        return entity?.toModel()
    }

    suspend fun storeProfile(profile: Profile) {
        val entity = profile.toEntity()
        profileDao.insertProfiles(entity)
    }

    suspend fun deleteDuplicates() {
        profileDao.deleteDuplicates()
    }
}
package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.Repository.ProfileRepository
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.launch

/**
 * Created by: Juan Manuel Jáuregui
 */

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseServicesFacade = FirebaseServicesFacade()
    val profile = mutableStateListOf<Profile>()
    val isLoading = mutableStateOf(false)

    private val db = AppLocalDatabase.getDatabase(application)
    private val profileRepository = ProfileRepository(db)

    init {
        loadProfileFromFirebase()
    }

    private fun loadProfileFromFirebase() {
        isLoading.value = true
        viewModelScope.launch {
            val profile = firebaseServicesFacade.fetchUserProfile()
            if (profile != null) {
                profileRepository.storeProfile(profile)
                profileRepository.deleteDuplicates()
                this@ProfileViewModel.profile.add(profile)
            }
            isLoading.value = false
        }
    }
}

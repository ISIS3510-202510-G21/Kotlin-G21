package com.isis3510.growhub.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    val profile = mutableStateListOf<Profile>()

    init {
        loadProfileFromFirebase()
    }

    private fun loadProfileFromFirebase() {
        viewModelScope.launch {
            val profile = firebaseFacade.fetchUserProfile()
            if (profile != null) {
                this@ProfileViewModel.profile.add(profile)
            }
        }
    }
}

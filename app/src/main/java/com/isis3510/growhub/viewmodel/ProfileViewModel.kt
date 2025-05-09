package com.isis3510.growhub.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.launch

/**
 * Created by: Juan Manuel Jáuregui
 */

class ProfileViewModel(
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    val profile = mutableStateListOf<Profile>()
    val isLoading = mutableStateOf(false)

    init {
        loadProfileFromFirebase()
    }

    private fun loadProfileFromFirebase() {
        isLoading.value = true
        viewModelScope.launch {
            val profile = firebaseFacade.fetchUserProfile()
            if (profile != null) {
                this@ProfileViewModel.profile.add(profile)
            }
            isLoading.value = false
        }
    }
}

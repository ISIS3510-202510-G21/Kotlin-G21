package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class Profile (
    val name: String = "",
    val following: Int = 0,
    val followers: Int = 0,
    val aboutMe: String = "",
    val interests: List<String> = listOf(),
    val profilePictureUrl: String = ""
)

class ProfileViewModel : ViewModel() {
    val profile = mutableStateListOf<Profile>()

    init {
        loadMockProfile()
    }

    private fun loadMockProfile() {
        viewModelScope.launch {
            val mockData = listOf(
                Profile("Camilo Smith", 320, 325, "I like to code", listOf("Android", "Kotlin"), "https://example.com/profile")
            )
            profile.addAll(mockData.take(1))
        }
    }
}
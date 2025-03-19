package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.launch

/**
 * Created by: Juan Manuel Jáuregui
 */

class ProfileViewModel : ViewModel() {

    val profile = mutableStateListOf<Profile>()

    //private val auth = FirebaseAuth.getInstance()
    //private val db = FirebaseFirestore.getInstance()

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

//    private fun loadUserProfile() {
//        val user = auth.currentUser
//        if (user != null) {
//            viewModelScope.launch {
//                try {
//                    val snapshot = db.collection("profiles")
//                        .whereEqualTo("user_ref", user.uid)
//                        .limit(1)
//                        .get()
//                        .await()
//
//                    if (!snapshot.isEmpty) {
//                        val document = snapshot.documents[0]
//                        val fetchedProfile = Profile(
//                            name = document.getString("name") ?: "",
//                            following = document.getLong("following")?.toInt() ?: 0,
//                            followers = document.getLong("followers")?.toInt() ?: 0,
//                            aboutMe = document.getString("description") ?: "",
//                            interests = document.get("interests") as? List<String> ?: listOf(),
//                            profilePictureUrl = document.getString("picture") ?: ""
//                        )
//                        profile.value = fetchedProfile
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
}

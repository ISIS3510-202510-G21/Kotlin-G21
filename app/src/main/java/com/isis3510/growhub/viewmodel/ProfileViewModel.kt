package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Profile Data Class (meanwhile)
data class Profile (
    val name: String = "",
    val following: Int = 0,
    val followers: Int = 0,
    val aboutMe: String = "",
    val interests: List<String> = listOf(),
    val profilePictureUrl: String = ""
)

// Profile View Model
class ProfileViewModel : ViewModel() {

    // Profile List
    val profile = mutableStateListOf<Profile>()

    // Connection to Firebase
    //private val auth = FirebaseAuth.getInstance()
    //private val db = FirebaseFirestore.getInstance()

    init {
        loadMockProfile()
    }

    // Load Mock Profile
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
//                    // Query the Firestore collection where the user ID matches
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
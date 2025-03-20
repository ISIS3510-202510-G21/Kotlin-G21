package com.isis3510.growhub.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    val profile = mutableStateListOf<Profile>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadProfileFromFirebase()
    }

    private fun loadProfileFromFirebase() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            val userDocRef = db.collection("users").document(userId)

            val querySnapshot = db.collection("profiles")
                .whereEqualTo("user_ref", userDocRef)
                .get()
                .await()

            val profileDocument = querySnapshot.documents.firstOrNull()

            if (profileDocument != null) {
                val profileData = profileDocument.data ?: emptyMap()

                val profilePicture = profileData["profile_picture"] as? String ?: ""
                val description = profileData["description"] as? String ?: ""
                val interests = (profileData["interests"] as? List<DocumentReference>) ?: emptyList()
                val followers = (profileData["followers"] as? List<DocumentReference>) ?: emptyList()
                val following = (profileData["following"] as? List<DocumentReference>) ?: emptyList()

                // Extract number of items in followers and following lists
                val followersCount = followers.size
                val followingCount = following.size

                // Extract the interests name from the interests collection
                val interestsNames = interests.mapNotNull { interestRef ->
                    val interestDoc = interestRef.get().await()
                    interestDoc.getString("name")
                }

                // Extract user name from users collection
                val userRef = profileData["user_ref"] as? DocumentReference
                val userName = if (userRef != null) {
                    val userDoc = userRef.get().await()
                    userDoc.getString("name") ?: ""
                } else {
                    ""
                }

                val profileExtracted = Profile(
                    profilePicture = profilePicture,
                    description = description,
                    interests = interestsNames,
                    followers = followersCount,
                    following = followingCount,
                    name = userName
                )

                profile.clear()
                profile.add(profileExtracted)
            }
        }
    }
}

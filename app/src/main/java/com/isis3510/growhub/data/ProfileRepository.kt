package com.isis3510.growhub.data

import com.isis3510.growhub.model.objects.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUserProfile(onComplete: (Profile?) -> Unit) {
        val userId = auth.currentUser?.uid  // Get the logged-in user's UID

        if (userId != null) {
            db.collection("profiles").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profile = document.toObject(Profile::class.java)
                        onComplete(profile)
                    } else {
                        onComplete(null) // No profile found
                    }
                }
                .addOnFailureListener {
                    onComplete(null) // Error fetching profile
                }
        } else {
            onComplete(null) // User not logged in
        }
    }
}

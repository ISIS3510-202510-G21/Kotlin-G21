package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.Repository.FollowItemRepository
import com.isis3510.growhub.local.database.FollowLocalStore
import com.isis3510.growhub.model.objects.FollowItem
import com.isis3510.growhub.utils.ConnectionStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowViewModel(application: Application) : AndroidViewModel(application) {

    // Room setup
    private val db = FollowLocalStore.getDatabase(application)
    private val repo = FollowItemRepository(db)

    // Firestore
    private val firestore = FirebaseFirestore.getInstance()

    // Connectivity
    private val connectivityVm = ConnectivityViewModel(application)
    val isOnline: StateFlow<Boolean> = connectivityVm.networkStatus
        .map { it == ConnectionStatus.Available }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Error channel
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    // Auth
    private val auth = FirebaseAuth.getInstance()
    private val meUid get() = auth.currentUser?.uid ?: ""

    // Following list
    private val _followingItems = MutableStateFlow<List<FollowItem>>(emptyList())
    val followingItems: StateFlow<List<FollowItem>> = _followingItems

    // Followers list
    private val _followersItems = MutableStateFlow<List<FollowItem>>(emptyList())
    val followersItems: StateFlow<List<FollowItem>> = _followersItems

    // Suggestions
    private val _suggestionsItems = MutableStateFlow<List<FollowItem>>(emptyList())
    val suggestionsItems: StateFlow<List<FollowItem>> = _suggestionsItems


    init {
        viewModelScope.launch {
            isOnline.collectLatest { online ->
                if (online) {
                    loadFromFirestore()
                } else {
                    observeLocal()
                }
            }
        }
    }

    private fun observeLocal() {
        viewModelScope.launch {
            repo.getAllFollowing().collect { list ->
                _followingItems.value = list
            }
        }
        viewModelScope.launch {
            repo.getAllFollowers().collect { list ->
                _followersItems.value = list
            }
        }
    }

    private fun loadFromFirestore() {
        viewModelScope.launch {
            try {
                val userRef = firestore.collection("users").document(meUid)
                // Perfil del usuario actual, para obtener sus listas
                val myProfileSnap = firestore.collection("profiles")
                    .whereEqualTo("user_ref", userRef)
                    .limit(1)
                    .get().await()
                    .documents
                    .firstOrNull()

                if (myProfileSnap != null) {
                    val followingRefs = myProfileSnap.get("following") as? List<*>
                    val followersRefs = myProfileSnap.get("followers") as? List<*>

                    suspend fun buildItem(userRef: com.google.firebase.firestore.DocumentReference): FollowItem? {
                        // 1) Obtengo name desde users/{id}
                        val userSnap = userRef.get().await()
                        val name = userSnap.getString("name") ?: "Unnamed"
                        // 2) Busco el perfil de ese usuario en profiles
                        val profileSnap = firestore.collection("profiles")
                            .whereEqualTo("user_ref", userRef)
                            .limit(1)
                            .get().await()
                            .documents
                            .firstOrNull()
                        val headline = profileSnap?.getString("headline") ?: ""
                        val picture  = profileSnap?.getString("profile_picture") ?: ""
                        return FollowItem(
                            userId = userRef.id,
                            name = name,
                            headline = headline,
                            profilePicture = picture
                        )
                    }

                    // Construir listas
                    val followingList = followingRefs
                        ?.mapNotNull { it as? com.google.firebase.firestore.DocumentReference }
                        ?.mapNotNull { ref -> buildItem(ref) }
                        ?: emptyList()

                    val followersList = followersRefs
                        ?.mapNotNull { it as? com.google.firebase.firestore.DocumentReference }
                        ?.mapNotNull { ref -> buildItem(ref) }
                        ?: emptyList()

                    // Guardar en local
                    repo.insertFollowing(followingList)
                    repo.insertFollowers(followersList)

                    // Emitir a UI
                    _followingItems.value = followingList
                    _followersItems.value  = followersList

                    val excluded = followingList.map { it.userId }.toSet() + meUid
                    val allUserDocs = firestore.collection("users").get().await().documents
                    val candidates = allUserDocs
                        .mapNotNull { doc -> doc.id.takeUnless { it in excluded } }
                        .shuffled()
                        .take(5)

                    // 4) Construir sugerencias en paralelo
                    val suggestions = coroutineScope {
                        candidates.map { uid ->
                            async {
                                val uRef = firestore.collection("users").document(uid)
                                buildItem(uRef)
                            }
                        }.awaitAll().filterNotNull()
                    }

                    _suggestionsItems.value = suggestions
                }
            } catch (e: Exception) {
                _error.emit("Failed to load data: ${e.message}")
            }
        }
    }

    /** Helper para construir un FollowItem dado su userRef */
    private suspend fun buildItem(userRef: com.google.firebase.firestore.DocumentReference): FollowItem? {
        return try {
            val usr = userRef.get().await()
            val name = usr.getString("name") ?: return null

            val profSnap = firestore.collection("profiles")
                .whereEqualTo("user_ref", userRef)
                .limit(1).get().await()
                .documents.firstOrNull()

            FollowItem(
                userId = userRef.id,
                name = name,
                headline = profSnap?.getString("headline") ?: "",
                profilePicture = profSnap?.getString("profile_picture") ?: ""
            )
        } catch (_: Exception) {
            null
        }
    }

    fun toggleFollow(targetUid: String, follow: Boolean) {
        viewModelScope.launch {
            if (!isOnline.value) {
                _error.emit("Please check your connection to follow/unfollow someone")
                return@launch
            }
            try {
                // 1) documento de perfil del usuario actual
                val meProfileDoc = firestore.collection("profiles")
                    .whereEqualTo("user_ref", firestore.document("users/$meUid"))
                    .limit(1).get().await()
                    .documents.firstOrNull()
                    ?: throw IllegalStateException("My profile not found")

                // 2) documento de perfil del target
                val targetProfileDoc = firestore.collection("profiles")
                    .whereEqualTo("user_ref", firestore.document("users/$targetUid"))
                    .limit(1).get().await()
                    .documents.firstOrNull()
                    ?: throw IllegalStateException("Target profile not found")

                // 3) referencias a esos docs
                val myRef     = meProfileDoc.reference
                val targetRef = targetProfileDoc.reference

                val userRefMe     = firestore.document("users/$meUid")
                val userRefTarget = firestore.document("users/$targetUid")

                // 4) batch update
                firestore.runBatch { batch ->
                    if (follow) {
                        batch.update(myRef,   "following", com.google.firebase.firestore.FieldValue.arrayUnion(userRefTarget))
                        batch.update(targetRef,"followers", com.google.firebase.firestore.FieldValue.arrayUnion(userRefMe))
                    } else {
                        batch.update(myRef,   "following", com.google.firebase.firestore.FieldValue.arrayRemove(userRefTarget))
                        batch.update(targetRef,"followers", com.google.firebase.firestore.FieldValue.arrayRemove(userRefMe))
                    }
                }.await()

                // 5) refrescar listas
                loadFromFirestore()
            } catch (e: Exception) {
                // cualquier fallo llega aquí
                _error.emit("Could not ${if (follow) "follow" else "unfollow"} user: ${e.message}")
            }
        }
    }
}
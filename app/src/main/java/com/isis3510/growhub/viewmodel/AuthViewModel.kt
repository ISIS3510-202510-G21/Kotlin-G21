package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.AuthPreferences
import com.isis3510.growhub.model.objects.AppUser
import com.isis3510.growhub.model.objects.AuthUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val authPrefs: AuthPreferences by lazy { AuthPreferences(application) }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Checa si en Firebase hay un currentUser
     * o si en local storage está marcado como logueado.
     */
    fun isUserLoggedIn(): Boolean {
        val firebaseLoggedIn = firebaseAuth.currentUser != null
        val localLoggedIn = authPrefs.isUserLoggedIn()
        return firebaseLoggedIn && localLoggedIn
    }

    // Para actualizar el email en el uiState
    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    // Para actualizar la contraseña en el uiState
    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    // Nuevo: para actualizar el nombre
    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    // Nuevo: para actualizar la confirmación de contraseña
    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = newConfirm)
    }

    fun onUserRoleChange(newRole: String) {
        _uiState.value = _uiState.value.copy(userRole = newRole)
        authPrefs.setUserRole(newRole)
    }


    fun loginUser(onLoginSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            firebaseAuth.signInWithEmailAndPassword(
                state.email.trim(),
                state.password.trim()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""

                    // Recuperar el rol del usuario desde Firestore
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val userRole = document.getString("userType") ?: "Attendee" // Rol por defecto

                                // Guardamos el rol y el estado de sesión en AuthPreferences
                                authPrefs.setUserRole(userRole)
                                authPrefs.setUserLoggedIn(true)

                                // Actualizamos el estado de la UI
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    userRole = userRole
                                )

                                onLoginSuccess()
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "User data not found"
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = e.message
                            )
                        }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    //crear Ua¿suario en firebase

    fun registerUser(onRegisterSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(
                errorMessage = "Passwords do not match.",
                isLoading = false
            )
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            firebaseAuth.createUserWithEmailAndPassword(
                state.email.trim(),
                state.password.trim()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    val selectedRole = state.userRole ?: "Attendee" // Asegurar un rol

                    val newUser = AppUser(
                        email = state.email.trim(),
                        name = state.name,
                        userType = selectedRole,
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(newUser)
                        .addOnSuccessListener {
                            authPrefs.setUserLoggedIn(true)
                            authPrefs.setUserRole(selectedRole)

                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onRegisterSuccess()
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = e.message
                            )
                        }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
        authPrefs.setUserLoggedIn(false)
    }

    fun onRememberMeChange(it: Boolean) {
        // Mantener lógica si luego deseas guardar "Remember Me" en shared prefs
    }
}
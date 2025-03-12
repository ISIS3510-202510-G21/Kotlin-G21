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

    fun loginUser(onLoginSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            firebaseAuth.signInWithEmailAndPassword(
                state.email.trim(),
                state.password.trim()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Marca en las preferencias que el usuario está logueado
                    authPrefs.setUserLoggedIn(true)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onLoginSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Crear un usuario en Firebase Auth y guardarlo en Firestore.
     */
    fun registerUser(onRegisterSuccess: () -> Unit) {
        val state = _uiState.value

        // 1) Verificar que las contraseñas coincidan
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(
                errorMessage = "Passwords do not match.",
                isLoading = false
            )
            return
        }

        // 2) Iniciar registro
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            firebaseAuth.createUserWithEmailAndPassword(
                state.email.trim(),
                state.password.trim()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Actualizamos prefs
                    authPrefs.setUserLoggedIn(true)

                    val userId = task.result?.user?.uid ?: ""
                    // 3) Crear objeto AppUser con los datos
                    val newUser = AppUser(
                        email = state.email.trim(),
                        name = state.name,
                        userType = "host", // por defecto
                        username = ""      // opcional
                    )

                    // 4) Guardarlo en la colección "users" con el UID
                    firestore.collection("users")
                        .document(userId)
                        .set(newUser)
                        .addOnSuccessListener {
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

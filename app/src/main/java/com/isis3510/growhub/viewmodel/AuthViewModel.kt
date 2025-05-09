package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.AuthPreferences
import com.isis3510.growhub.model.objects.AuthUiState
import com.isis3510.growhub.model.objects.Skill
import com.isis3510.growhub.offline.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val authPrefs: AuthPreferences by lazy { AuthPreferences(application) }

    init {
        // Recuperar credenciales si "remember me" estaba encendido
        val savedEmail = authPrefs.getSavedEmail()
        val savedPass = authPrefs.getSavedPassword()
        if (!savedEmail.isNullOrBlank() && !savedPass.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                email = savedEmail,
                password = savedPass,
                rememberMe = true
            )
        }
    }

    // Funciones para actualizar la UI state
    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = newConfirm)
    }

    fun onUserRoleChange(newRole: String) {
        _uiState.value = _uiState.value.copy(userRole = newRole)
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = checked)
        if (!checked) {
            authPrefs.clearCredentials()
        }
    }

    fun isUserLoggedIn(): Boolean {
        val firebaseLoggedIn = firebaseAuth.currentUser != null
        val localLoggedIn = authPrefs.isUserLoggedIn()
        return firebaseLoggedIn && localLoggedIn
    }

    fun registerUser(onRegisterAuthSuccess: (String) -> Unit) {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        onRegisterAuthSuccess("")
    }

    fun updateRegistrationData(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        userRole: String
    ) {
        _uiState.value = _uiState.value.copy(
            name = name,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            userRole = userRole
        )
    }

    fun loginUser(onLoginSuccess: () -> Unit) {
        val state = _uiState.value
        val context = getApplication<Application>().applicationContext

        // Verificamos internet
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.value = state.copy(errorMessage = "Please check your internet connection.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            firebaseAuth.signInWithEmailAndPassword(
                state.email.trim(),
                state.password.trim()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    authPrefs.setUserLoggedIn(true)
                    if (_uiState.value.rememberMe) {
                        authPrefs.saveCredentials(state.email.trim(), state.password.trim())
                    }
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

    fun fetchSkillsList() {
        val context = getApplication<Application>().applicationContext
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return
        }
        firestore.collection("skills").get().addOnSuccessListener { result ->
            val skills = result.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                Skill(id = doc.id, name = name)
            }
            _uiState.value = _uiState.value.copy(availableSkills = skills)
        }.addOnFailureListener { e ->
            _uiState.value = _uiState.value.copy(errorMessage = e.message)
        }
    }

    /**
     * Aquí se realiza el registro real en FirebaseAuth (createUserWithEmailAndPassword)
     * y se crea el documento en Firestore para el usuario.
     */
    fun finalizeUserRegistration(
        selectedSkills: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value
        val context = getApplication<Application>().applicationContext

        // Validar conexión
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError("Please check your internet connection.")
            return
        }

        // Validar contraseñas coincidan
        if (state.password != state.confirmPassword) {
            onError("Passwords do not match.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        // Crear el usuario en FirebaseAuth
        firebaseAuth.createUserWithEmailAndPassword(
            state.email.trim(),
            state.password.trim()
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newUserId = task.result?.user?.uid.orEmpty()

                val userData = hashMapOf(
                    "email" to state.email.trim(),
                    "name" to state.name.trim(),
                    "user_type" to if (state.userRole.isBlank()) "Attendee" else state.userRole,
                    "skills" to selectedSkills
                )

                // Guardar en Firestore
                firestore.collection("users").document(newUserId)
                    .set(userData)
                    .addOnSuccessListener {
                        // Manejo de "rememberMe"
                        if (state.rememberMe) {
                            authPrefs.saveCredentials(state.email.trim(), state.password.trim())
                            authPrefs.setUserLoggedIn(true)
                        } else {
                            authPrefs.clearCredentials()
                            authPrefs.setUserLoggedIn(false)
                        }

                        // Guardar datos básicos en local (userId, nombre, email)
                        authPrefs.saveUserData(
                            userId = newUserId,
                            userName = state.name.trim(),
                            userEmail = state.email.trim()
                        )

                        _uiState.value = _uiState.value.copy(
                            userId = newUserId,
                            isLoading = false,
                            errorMessage = null
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                        onError(e.message ?: "Unknown error")
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = task.exception?.message ?: "Unknown error"
                )
                onError(task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
        authPrefs.setUserLoggedIn(false)
        authPrefs.clearCredentials()
        _uiState.value = AuthUiState()
    }

    /**
     * Permite asignar un userId manualmente si fuera necesario en alguna lógica anterior.
     */
    fun setUserId(id: String) {
        _uiState.value = _uiState.value.copy(userId = id)
    }
}

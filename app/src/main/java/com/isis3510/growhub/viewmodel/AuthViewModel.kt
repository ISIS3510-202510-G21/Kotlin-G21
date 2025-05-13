package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.cache.RegistrationCache
import com.isis3510.growhub.model.AuthPreferences
import com.isis3510.growhub.model.objects.AuthUiState
import com.isis3510.growhub.model.objects.Skill
import com.isis3510.growhub.offline.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    /* ------------------ STATE ------------------ */
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /* ------------------ SERVICIOS ------------------ */
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore    by lazy { FirebaseFirestore.getInstance() }
    private val authPrefs    by lazy { AuthPreferences(application) }

    init {
        // 1.  Intenta restaurar un borrador de registro (en RAM)
        RegistrationCache.get<AuthUiState>("draft")?.let { _uiState.value = it }

        // 2.  Prefill credenciales guardadas *de LOGIN* (recordar‑me)
        authPrefs.getLoginEmail()?.let { email ->
            authPrefs.getLoginPassword()?.let { pass ->
                _uiState.value = _uiState.value.copy(
                    email      = email,
                    password   = pass,
                    rememberMe = true
                )
            }
        }
    }

    /* ------------------ Mutadores sencillos ------------------ */
    /* ---------- al comienzo de la clase ---------- */
    private fun cacheDraft() {
        RegistrationCache.put("draft", _uiState.value)
    }

    /* ---------- reemplaza los mutadores ---------- */
    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
        cacheDraft()
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
        cacheDraft()
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
        cacheDraft()
    }

    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = newConfirm)
        cacheDraft()
    }

    fun onUserRoleChange(newRole: String) {
        _uiState.value = _uiState.value.copy(userRole = newRole)
        cacheDraft()
    }


    fun onRememberMeChange(checked: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = checked)
        if (!checked) authPrefs.clearLoginCredentials()
    }

    /* ------------------ UTILIDAD ------------------ */
    fun isUserLoggedIn(): Boolean =
        firebaseAuth.currentUser != null && authPrefs.isUserLoggedIn()

    /* ==========================================================
     *  LOGIN
     * ========================================================== */
    fun loginUser(onLoginSuccess: () -> Unit) {
        val state   = _uiState.value
        val context = getApplication<Application>().applicationContext

        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.value = state.copy(errorMessage = "Please check your internet connection.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            firebaseAuth.signInWithEmailAndPassword(state.email.trim(), state.password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        authPrefs.setUserLoggedIn(true)

                        if (_uiState.value.rememberMe)
                            authPrefs.saveLoginCredentials(state.email.trim(), state.password.trim())

                        onLoginSuccess()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading    = false,
                            errorMessage = task.exception?.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    /* ==========================================================
     *  REGISTRO — Paso 1: guardar borrador y cache
     * ========================================================== */
    fun updateRegistrationData(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        userRole: String
    ) {
        val newState = _uiState.value.copy(
            name            = name,
            email           = email,
            password        = password,
            confirmPassword = confirmPassword,
            userRole        = userRole
        )
        _uiState.value = newState

        // Guarda borrador (RAM) + credenciales de registro (SharedPrefs)
        RegistrationCache.put("draft", newState)
        authPrefs.saveRegisterCredentials(email.trim(), password.trim())
    }

    /* ==========================================================
     *  REGISTRO — Paso 2: creación en Firebase
     * ========================================================== */
    fun finalizeUserRegistration(
        selectedSkills: List<String>,
        onSuccess: () -> Unit,
        onError:  (String) -> Unit
    ) {
        val draft    = RegistrationCache.get<AuthUiState>("draft") ?: _uiState.value
        val context  = getApplication<Application>().applicationContext

        if (!NetworkUtils.isNetworkAvailable(context)) { onError("Please check your internet connection."); return }
        if (draft.password != draft.confirmPassword)   { onError("Passwords do not match."); return }

        _uiState.value = draft.copy(isLoading = true, errorMessage = null)

        firebaseAuth.createUserWithEmailAndPassword(draft.email.trim(), draft.password.trim())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false,
                        errorMessage = task.exception?.message ?: "Unknown error")
                    onError(task.exception?.message ?: "Unknown error")
                    return@addOnCompleteListener
                }

                /* ---------------- FirebaseAuth OK ---------------- */
                val newUserId = task.result?.user?.uid.orEmpty()
                val userData  = hashMapOf(
                    "email"     to draft.email.trim(),
                    "name"      to draft.name.trim(),
                    "user_type" to draft.userRole.ifBlank { "Attendee" },
                    "skills"    to selectedSkills
                )

                firestore.collection("users").document(newUserId)
                    .set(userData)
                    .addOnSuccessListener {
                        /* —— persistencia local —— */
                        if (draft.rememberMe) {
                            authPrefs.saveLoginCredentials(draft.email.trim(), draft.password.trim())
                            authPrefs.setUserLoggedIn(true)
                        } else {
                            authPrefs.clearLoginCredentials()
                            authPrefs.setUserLoggedIn(false)
                        }
                        authPrefs.saveUserData(newUserId, draft.name.trim(), draft.email.trim())

                        /* —— limpieza y éxito —— */
                        RegistrationCache.clear()
                        authPrefs.clearRegisterCredentials()

                        _uiState.value = _uiState.value.copy(
                            userId      = newUserId,
                            isLoading   = false,
                            errorMessage = null
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                        onError(e.message ?: "Unknown error")
                    }
            }
    }

    /* ==========================================================
     *  AUX — fetch skills, logout, etc.  (sin cambios relevantes)
     * ========================================================== */
    fun fetchSkillsList() {
        val context = getApplication<Application>().applicationContext
        if (!NetworkUtils.isNetworkAvailable(context)) return

        firestore.collection("skills").get()
            .addOnSuccessListener { result ->
                val skills = result.documents.mapNotNull { doc ->
                    doc.getString("name")?.let { Skill(id = doc.id, name = it) }
                }
                _uiState.value = _uiState.value.copy(availableSkills = skills)
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
        authPrefs.setUserLoggedIn(false)
        authPrefs.clearLoginCredentials()
        RegistrationCache.clear()
        _uiState.value = AuthUiState()
    }

    /* ==========================================================
     *  Reinicio limpio de la pantalla Register
     * ========================================================== */
    fun startFreshRegistration() {
        val regEmail = authPrefs.getRegisterEmail()
        val regPass  = authPrefs.getRegisterPassword()

        _uiState.value = _uiState.value.copy(
            name            = "",
            email           = regEmail.orEmpty(),
            password        = regPass.orEmpty(),
            confirmPassword = "",
            userRole        = "",
            errorMessage    = null,
            isLoading       = false
        )
        RegistrationCache.remove("draft")
    }

    /* ------------- en el bloque de propiedades ------------- */
    private var cachedLoginEmail:    String? = null
    private var cachedLoginPassword: String? = null

    /* ------------- nuevo: guarda el estado actual de LOGIN ------------- */
    fun preserveLoginFields() {
        cachedLoginEmail    = _uiState.value.email
        cachedLoginPassword = _uiState.value.password
    }

    /* ------------- nuevo: restaura el estado previo de LOGIN ------------- */
    fun restoreLoginFields() {
        _uiState.value = _uiState.value.copy(
            email    = cachedLoginEmail.orEmpty(),
            password = cachedLoginPassword.orEmpty()
        )
    }

    fun hasRegistrationDraft(): Boolean =
        com.isis3510.growhub.cache.RegistrationCache
            .get<com.isis3510.growhub.model.objects.AuthUiState>("draft") != null

}






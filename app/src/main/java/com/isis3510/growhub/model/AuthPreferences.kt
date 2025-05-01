package com.isis3510.growhub.model

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "auth_prefs"
private const val KEY_IS_LOGGED_IN = "is_user_logged_in"

// Claves para credenciales email & password
private const val KEY_SAVED_EMAIL = "saved_email"
private const val KEY_SAVED_PASSWORD = "saved_password"

// Claves adicionales para almacenar userId y nombre
private const val KEY_USER_ID = "user_id"
private const val KEY_USER_NAME = "user_name"

class AuthPreferences(context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Mantener si está logueado (boolean)
    fun setUserLoggedIn(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveCredentials(email: String, password: String) {
        sharedPrefs.edit()
            .putString(KEY_SAVED_EMAIL, email)
            .putString(KEY_SAVED_PASSWORD, password)
            .apply()
    }

    fun getSavedEmail(): String? {
        return sharedPrefs.getString(KEY_SAVED_EMAIL, null)
    }

    fun getSavedPassword(): String? {
        return sharedPrefs.getString(KEY_SAVED_PASSWORD, null)
    }

    fun clearCredentials() {
        sharedPrefs.edit()
            .remove(KEY_SAVED_EMAIL)
            .remove(KEY_SAVED_PASSWORD)
            .apply()
    }

    /**
     * Guarda datos adicionales del usuario: ID, nombre y email (para acceso rápido en local).
     */
    fun saveUserData(userId: String, userName: String, userEmail: String) {
        sharedPrefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_SAVED_EMAIL, userEmail) // Reutiliza la clave de email si quieres
            .apply()
    }

    fun getUserId(): String? {
        return sharedPrefs.getString(KEY_USER_ID, null)
    }

    fun getUserName(): String? {
        return sharedPrefs.getString(KEY_USER_NAME, null)
    }
}

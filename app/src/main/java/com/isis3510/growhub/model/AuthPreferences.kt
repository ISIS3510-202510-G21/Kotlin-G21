package com.isis3510.growhub.model

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "auth_prefs"
private const val KEY_IS_LOGGED_IN = "is_user_logged_in"
private const val KEY_USER_ROLE = "user_role"

// 🔴 NUEVAS CONSTANTES PARA GUARDAR EMAIL Y PASSWORD
private const val KEY_SAVED_EMAIL = "saved_email"
private const val KEY_SAVED_PASSWORD = "saved_password"

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

    // 🔴 NUEVAS FUNCIONES PARA GUARDAR/OBTENER/BORRAR EMAIL Y PASSWORD
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
}

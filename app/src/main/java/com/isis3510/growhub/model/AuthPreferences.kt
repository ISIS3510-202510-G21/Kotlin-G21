package com.isis3510.growhub.model

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "auth_prefs"
private const val KEY_IS_LOGGED_IN = "is_user_logged_in"

/**
 * Clase para gestionar la preferencia local que indica si el usuario
 * inició sesión exitosamente y desea mantener la sesión abierta.
 */
class AuthPreferences(context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setUserLoggedIn(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
}

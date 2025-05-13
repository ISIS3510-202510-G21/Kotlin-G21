package com.isis3510.growhub.model

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME          = "auth_prefs"
private const val KEY_IS_LOGGED_IN    = "is_user_logged_in"

/* ----------------‑NUEVAS CLAVES SEPARADAS‑---------------- */
private const val REGISTER_EMAIL      = "register_email"
private const val REGISTER_PASSWORD   = "register_password"
private const val LOGIN_EMAIL         = "login_email"
private const val LOGIN_PASSWORD      = "login_password"

/* --------‑Datos adicionales que ya existían‑-------- */
private const val KEY_USER_ID         = "user_id"
private const val KEY_USER_NAME       = "user_name"

class AuthPreferences(context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /* ---------- Sesión ---------- */
    fun setUserLoggedIn(loggedIn: Boolean) =
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()

    fun isUserLoggedIn(): Boolean =
        sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)

    /* ---------- LOGIN (creds recordadas) ---------- */
    fun saveLoginCredentials(email: String, password: String) =
        sharedPrefs.edit()
            .putString(LOGIN_EMAIL, email)
            .putString(LOGIN_PASSWORD, password)
            .apply()

    fun getLoginEmail(): String?     = sharedPrefs.getString(LOGIN_EMAIL, null)
    fun getLoginPassword(): String?  = sharedPrefs.getString(LOGIN_PASSWORD, null)

    fun clearLoginCredentials() =
        sharedPrefs.edit()
            .remove(LOGIN_EMAIL)
            .remove(LOGIN_PASSWORD)
            .apply()

    /* ---------- REGISTER (borrador) ---------- */
    fun saveRegisterCredentials(email: String, password: String) =
        sharedPrefs.edit()
            .putString(REGISTER_EMAIL, email)
            .putString(REGISTER_PASSWORD, password)
            .apply()

    fun getRegisterEmail(): String?     = sharedPrefs.getString(REGISTER_EMAIL, null)
    fun getRegisterPassword(): String?  = sharedPrefs.getString(REGISTER_PASSWORD, null)

    fun clearRegisterCredentials() =
        sharedPrefs.edit()
            .remove(REGISTER_EMAIL)
            .remove(REGISTER_PASSWORD)
            .apply()

    /* ---------- Datos extra opcionales ---------- */
    fun saveUserData(userId: String, userName: String, userEmail: String) =
        sharedPrefs.edit()
            .putString(KEY_USER_ID,   userId)
            .putString(KEY_USER_NAME, userName)
            .putString(LOGIN_EMAIL,   userEmail)   // sincrónicamente actualizamos email de login
            .apply()

    fun getUserId(): String?   = sharedPrefs.getString(KEY_USER_ID,   null)
    fun getUserName(): String? = sharedPrefs.getString(KEY_USER_NAME, null)
}

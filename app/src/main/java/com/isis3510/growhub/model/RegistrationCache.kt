package com.isis3510.growhub.cache

import android.util.LruCache

/**
 * Caché LRU en memoria para el flujo de registro.
 *
 * Claves usadas actualmente:
 *  • "draft"  → AuthUiState con los datos que el usuario digitó en RegisterScreen.
 *
 * Se usa sólo mientras la app está en memoria; si el proceso muere, se recrea
 * desde cero (es lo que la profesora pidió: caché temporal en memoria).
 */
object RegistrationCache {
    private const val MAX_ENTRIES = 20
    private val cache = object : LruCache<String, Any>(MAX_ENTRIES) {}

    fun put(key: String, value: Any) = cache.put(key, value)

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = cache.get(key) as? T

    fun clear() = cache.evictAll()
}

package com.isis3510.growhub.utils

import android.util.LruCache

object ProfileCache {
    private const val MAX_SIZE = 1

    private val cache = object : LruCache<String, Map<String, Any>>(MAX_SIZE) {}

    fun put(userId: String, profile: Map<String, Any>) {
        cache.put(userId, profile)
    }

    fun get(userId: String): Map<String, Any>? {
        return cache.get(userId)
    }

    fun clear() {
        cache.evictAll()
    }
}

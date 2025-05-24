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

object AttendeeStatsCache {
    private const val MAX_ENTRIES = 10

    private val headlineCache = object : LruCache<String, String>(MAX_ENTRIES) {}
    private val interestCache = object : LruCache<String, String>(MAX_ENTRIES) {}

    fun getHeadline(key: String): String? = headlineCache.get(key)
    fun getInterest(key: String): String? = interestCache.get(key)

    fun putHeadline(key: String, value: String) {
        headlineCache.put(key, value)
    }

    fun putInterest(key: String, value: String) {
        interestCache.put(key, value)
    }
}


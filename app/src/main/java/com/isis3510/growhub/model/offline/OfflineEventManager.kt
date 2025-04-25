package com.isis3510.growhub.offline

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.isis3510.growhub.Repository.CreateEventRepository

class OfflineEventManager(
    context: Context,
    private val createEventRepository: CreateEventRepository
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("offline_events_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveOfflineEvent(
        name: String,
        cost: Double,
        category: String,
        description: String,
        startDate: Timestamp,
        endDate: Timestamp,
        locationId: String,
        imageUrl: String?,
        address: String,
        details: String,
        city: String,
        isUniversity: Boolean,
        skillIds: List<String>
    ) {
        val offlineEvents = getOfflineEvents().toMutableList()
        offlineEvents.add(
            OfflineEvent(
                name = name,
                cost = cost,
                category = category,
                description = description,
                startDate = startDate,
                endDate = endDate,
                locationId = locationId,
                imageUrl = imageUrl,
                address = address,
                details = details,
                city = city,
                isUniversity = isUniversity,
                skillIds = skillIds
            )
        )
        val json = gson.toJson(offlineEvents)
        prefs.edit().putString("OFFLINE_EVENTS", json).apply()
    }

    fun getOfflineEvents(): List<OfflineEvent> {
        val json = prefs.getString("OFFLINE_EVENTS", null) ?: return emptyList()
        val type = object : TypeToken<List<OfflineEvent>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun clearOfflineEvents() {
        prefs.edit().remove("OFFLINE_EVENTS").apply()
    }

    fun updateOfflineEvents(events: List<OfflineEvent>) {
        val json = gson.toJson(events)
        prefs.edit().putString("OFFLINE_EVENTS", json).apply()
    }

    suspend fun tryUploadAllOfflineEvents(): Int {
        var uploadedCount = 0
        val currentList = getOfflineEvents().toMutableList()
        val iterator = currentList.listIterator()

        while (iterator.hasNext()) {
            val event = iterator.next()
            val success = createEventRepository.createEvent(
                name = event.name,
                cost = event.cost,
                category = event.category,
                description = event.description,
                startDate = event.startDate,
                endDate = event.endDate,
                locationId = event.locationId,
                imageUrl = event.imageUrl,
                address = event.address,
                details = event.details,
                city = event.city,
                isUniversity = event.isUniversity,
                skillIds = event.skillIds
            )
            if (success) {
                uploadedCount++
                iterator.remove()
            }
        }

        if (uploadedCount > 0) {
            updateOfflineEvents(currentList)
        }
        return uploadedCount
    }

    suspend fun uploadSingleEvent(
        name: String,
        cost: Double,
        category: String,
        description: String,
        startDate: Timestamp,
        endDate: Timestamp,
        locationId: String,
        imageUrl: String?,
        address: String,
        details: String,
        city: String,
        isUniversity: Boolean,
        skillIds: List<String>
    ): Boolean {
        return createEventRepository.createEvent(
            name = name,
            cost = cost,
            category = category,
            description = description,
            startDate = startDate,
            endDate = endDate,
            locationId = locationId,
            imageUrl = imageUrl,
            address = address,
            details = details,
            city = city,
            isUniversity = isUniversity,
            skillIds = skillIds
        )
    }
}

package com.isis3510.growhub.offline

import android.content.Context
import com.google.firebase.Timestamp
import com.isis3510.growhub.Repository.CreateEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class OfflineEventManager(
    private val context: Context,
    private val createEventRepository: CreateEventRepository
) {
    private val offlineEventsFile = File(context.filesDir, "offline_events.json")

    init {
        // Ensure the file exists
        if (!offlineEventsFile.exists()) {
            offlineEventsFile.writeText("[]")
        }
    }

    suspend fun saveOfflineEvent(
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
        skillIds: List<String>,
        latitude: Double? = null,
        longitude: Double? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val currentEventsContent = offlineEventsFile.readText()
            val eventsArray = JSONArray(currentEventsContent)

            val newEvent = JSONObject().apply {
                put("name", name)
                put("cost", cost)
                put("category", category)
                put("description", description)
                put("startDate", startDate.seconds)
                put("endDate", endDate.seconds)
                put("locationId", locationId)
                put("imageUrl", imageUrl ?: "")
                put("address", address)
                put("details", details)
                put("city", city)
                put("isUniversity", isUniversity)
                put("skillIds", JSONArray(skillIds))
                // Add latitude and longitude if available
                if (latitude != null) put("latitude", latitude)
                if (longitude != null) put("longitude", longitude)
            }

            eventsArray.put(newEvent)
            offlineEventsFile.writeText(eventsArray.toString())
            return@withContext true
        } catch (e: Exception) {
            // Log error
            return@withContext false
        }
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
        skillIds: List<String>,
        latitude: Double? = null,
        longitude: Double? = null
    ): Boolean {
        return try {
            createEventRepository.createEvent(
                name, cost, category, description, startDate, endDate,
                locationId, imageUrl, address, details, city, isUniversity, skillIds,
                latitude, longitude
            )
        } catch (e: Exception) {
            // Log error
            false
        }
    }

    suspend fun tryUploadAllOfflineEvents(): Int = withContext(Dispatchers.IO) {
        if (!offlineEventsFile.exists() || !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext 0
        }

        try {
            val currentEventsContent = offlineEventsFile.readText()
            val eventsArray = JSONArray(currentEventsContent)

            var uploadedCount = 0
            val remainingEvents = JSONArray()

            for (i in 0 until eventsArray.length()) {
                val eventObj = eventsArray.getJSONObject(i)

                val uploadSuccess = createEventRepository.createEvent(
                    name = eventObj.getString("name"),
                    cost = eventObj.getDouble("cost"),
                    category = eventObj.getString("category"),
                    description = eventObj.getString("description"),
                    startDate = Timestamp(eventObj.getLong("startDate"), 0),
                    endDate = Timestamp(eventObj.getLong("endDate"), 0),
                    locationId = eventObj.getString("locationId"),
                    imageUrl = eventObj.getString("imageUrl"),
                    address = eventObj.getString("address"),
                    details = eventObj.getString("details"),
                    city = eventObj.getString("city"),
                    isUniversity = eventObj.getBoolean("isUniversity"),
                    skillIds = parseSkillIds(eventObj.getJSONArray("skillIds")),
                    latitude = if (eventObj.has("latitude")) eventObj.getDouble("latitude") else null,
                    longitude = if (eventObj.has("longitude")) eventObj.getDouble("longitude") else null
                )

                if (uploadSuccess) {
                    uploadedCount++
                } else {
                    remainingEvents.put(eventObj)
                }
            }

            // Save back any events that failed to upload
            offlineEventsFile.writeText(remainingEvents.toString())

            return@withContext uploadedCount
        } catch (e: Exception) {
            // Log error
            return@withContext 0
        }
    }

    private fun parseSkillIds(jsonArray: JSONArray): List<String> {
        val result = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            result.add(jsonArray.getString(i))
        }
        return result
    }
}

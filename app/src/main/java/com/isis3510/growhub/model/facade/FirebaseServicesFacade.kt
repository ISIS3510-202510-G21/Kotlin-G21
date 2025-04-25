package com.isis3510.growhub.model.facade

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.isis3510.growhub.model.filter.Filter
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.Profile
import kotlinx.coroutines.tasks.await
import java.time.ZoneId

/**
 * Facade central para todas las lecturas a Firestore.
 *
 * ▸ Mantiene los métodos **simples** usados por tu versión HEAD (devuelven List\<Event>)  
 * ▸ Incluye los métodos **paginados** y helpers añadidos en la rama develop  
 * ▸ No rompe ningún código existente.
 */
class FirebaseServicesFacade(private val filter: Filter = Filter()) {

    /* ------------------------------------------------------------------ */
    /*  PERFIL                                                            */
    /* ------------------------------------------------------------------ */
    suspend fun fetchUserProfile(): Profile? = try {
        val data = filter.getProfileData()      // develop
        if (data == null) return null

        // Si existen referencias de intereses / followers, las resolvemos como en HEAD
        val interestsNames: List<String> = when (val interests = data["interests"]) {
            is List<*> && interests.firstOrNull() is DocumentReference -> {
                interests.mapNotNull { (it as DocumentReference).get().await().getString("name") }
            }
            is List<*> -> interests.filterIsInstance<String>()
            else -> emptyList()
        }

        val followersCount: Int = when (val followers = data["followers"]) {
            is List<*> -> followers.size                               // HEAD
            is Int     -> followers                                    // develop
            else       -> 0
        }

        val followingCount: Int = when (val following = data["following"]) {
            is List<*> -> following.size
            is Int     -> following
            else       -> 0
        }

        val name: String = when (val maybeName = data["name"]) {
            is String -> maybeName
            else -> {
                // HEAD guardaba el nombre dentro de user_ref
                (data["user_ref"] as? DocumentReference)
                    ?.get()
                    ?.await()
                    ?.getString("name") ?: ""
            }
        }

        Profile(
            profilePicture = data["profilePicture"] as? String ?: "",
            description    = data["description"]    as? String ?: "",
            interests      = interestsNames,
            followers      = followersCount,
            following      = followingCount,
            name           = name
        )
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching user profile", e)
        null
    }

    /* ------------------------------------------------------------------ */
    /*  EVENTOS – helpers comunes                                          */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun mapFilterEventsToEvents(filteredEvents: List<Map<String, Any>>): List<Event> {
        val events = mutableListOf<Event>()
        for (event in filteredEvents) {
            val id          = event["id"]       as? String ?: ""
            val name        = event["name"]     as? String ?: ""
            val imageUrl    = event["image"]    as? String ?: ""
            val description = event["description"] as? String ?: ""
            val cost        = (event["cost"]    as? Number)?.toInt() ?: 0

            /* asistentes ------------------------------------------------- */
            val attendeesRefs = (event["attendees"] as? List<DocumentReference>) ?: emptyList()
            val attendeesNames = attendeesRefs.mapNotNull {
                it.get().await().getString("name")
            }

            /* creador ---------------------------------------------------- */
            val creatorName = (event["creator_id"] as? DocumentReference)
                ?.get()
                ?.await()
                ?.getString("name") ?: ""

            /* categoría -------------------------------------------------- */
            val categoryName = (event["category"] as? DocumentReference)
                ?.get()
                ?.await()
                ?.getString("name") ?: ""

            /* ubicación -------------------------------------------------- */
            val locationRef = event["location_id"] as? DocumentReference
            val locationDoc = locationRef?.get()?.await()
            val locationName = locationDoc?.getString("address") ?: ""
            val locationCity = locationDoc?.getString("city") ?: ""
            val isUniversity = locationDoc?.getBoolean("university") ?: false

            /* skills ----------------------------------------------------- */
            val skillRefs = (event["skills"] as? List<DocumentReference>) ?: emptyList()
            val skills = skillRefs.mapNotNull {
                it.get().await().getString("name")
            }

            /* fechas ----------------------------------------------------- */
            val fmt     = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val startTs = event["start_date"] as? Timestamp
            val endTs   = event["end_date"]   as? Timestamp
            val start   = startTs?.toDate()?.toInstant()
                ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            val end     = endTs?.toDate()?.toInstant()
                ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

            events += Event(
                id          = id,
                name        = name,
                imageUrl    = imageUrl,
                description = description,
                cost        = cost,
                attendees   = attendeesNames,
                startDate   = start?.format(fmt).toString(),
                endDate     = end?.format(fmt).toString(),
                category    = categoryName,
                location    = locationName,
                city        = locationCity,
                isUniversity= isUniversity,
                skills      = skills,
                creator     = creatorName
            )
        }
        return events
    }

    /* ------------------------------------------------------------------ */
    /*  EVENTOS – versiones paginadas (develop)                            */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeEvents(
        limit: Long = 5
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getHomeEventsData(limit)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching home events", e)
        Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextHomeEventsData(limit, lastSnapshot)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching next home events", e)
        Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeRecommendedEvents(
        limit: Long = 5
    ): Pair<List<Event>, Set<String>> = try {
        val (raw, offsetIds) = filter.getHomeRecommendedEventsData(limit)
        Pair(mapFilterEventsToEvents(raw), offsetIds)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching recommended events", e)
        Pair(emptyList(), emptySet())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeRecommendedEvents(
        limit: Long = 3,
        offsetIds: Set<String>
    ): List<Event> = try {
        mapFilterEventsToEvents(filter.getNextHomeRecommendedEventsData(limit, offsetIds))
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching next recommended events", e)
        emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEvents(
        limit: Long = 25
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getMyEventsData(limit)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching my events", e)
        Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextMyEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextMyEventsData(limit, lastSnapshot)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching next my events", e)
        Pair(emptyList(), null)
    }

    /* ------------------------------------------------------------------ */
    /*  EVENTOS – métodos *compatibles* con HEAD (sin paginación)         */
    /*  (simplemente devuelven el .first de los métodos superiores)       */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeEvents(): List<Event> =
        fetchHomeEvents(limit = 5).first

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeRecommendedEvents(): List<Event> =
        fetchHomeRecommendedEvents(limit = 5).first

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEvents(): List<Event> =
        fetchMyEvents(limit = 25).first

    /* ------------------------------------------------------------------ */
    /*  EVENTOS – create / registration extras (develop)                  */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEventsCreate(
        limit: Long = 25
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getMyEventsCreateData(limit)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching my events create", e)
        Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchRegistrationData(eventID: String): Event = try {
        val raw = filter.getRegistrationData(eventID)

        /* Re-uso del mapper para no duplicar lógica */
        mapFilterEventsToEvents(listOf(raw)).first().copy(id = eventID)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching registration data", e)
        throw e
    }

    /* ------------------------------------------------------------------ */
    /*  SEARCH (develop)                                                  */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchSearchEvents(
        limit: Long = 5
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getSearchEventsData(limit)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching search events", e)
        Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextSearchEvents(
        limit: Long = 3,
        lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextSearchEventsData(limit, lastSnapshot)
        Pair(mapFilterEventsToEvents(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching next search events", e)
        Pair(emptyList(), null)
    }

    /* ------------------------------------------------------------------ */
    /*  CATEGORÍAS, HABILIDADES, UBICACIONES                              */
    /* ------------------------------------------------------------------ */
    suspend fun fetchCategories(): List<Category> = try {
        filter.getCategoriesData().mapNotNull { map ->
            (map["name"] as? String)?.let { Category(it) }
        }
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching categories", e)
        emptyList()
    }

    suspend fun fetchSkills(): List<String> = try {
        filter.getSkillsData().mapNotNull { it["name"] as? String }
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching skills", e)
        emptyList()
    }

    suspend fun fetchLocations(): List<String> = try {
        val cities = mutableSetOf<String>()
        filter.getLocationsData().forEach { loc ->
            (loc["city"] as? String)?.let { cities.add(it) }
        }
        cities.toList()
    } catch (e: Exception) {
        Log.e("FirebaseServicesFacade", "Error fetching locations", e)
        emptyList()
    }
}

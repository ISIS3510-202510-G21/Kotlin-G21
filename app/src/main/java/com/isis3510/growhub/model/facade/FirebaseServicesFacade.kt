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
 * Facade central – unifica lo que había en HEAD y develop.
 *
 *  • Todos los métodos “paginados” devuelven **Pair<List<Event>, DocumentSnapshot?>**.
 *  • Se mantienen los wrappers simples (`fetchHomeEvents()`, `fetchMyEvents()`, …)
 *    que sólo retornan la `List<Event>` para no romper código previo.
 *  • El modelo `Event` **ya no** lleva `city`; la ciudad sigue estando en `Location`.
 */
class FirebaseServicesFacade(
    private val filter: Filter = Filter()
) {

    /* ------------------------------------------------------------------ */
    /*  PERFIL                                                            */
    /* ------------------------------------------------------------------ */
    suspend fun fetchUserProfile(): Profile? {
        return try {
            val data = filter.getProfileData() ?: return null

            /* intereses: pueden ser refs (HEAD antiguo) o Strings (develop) */
            val interests: List<String> = when (val raw = data["interests"]) {
                is List<*> -> if (raw.firstOrNull() is DocumentReference)
                    raw.mapNotNull { (it as DocumentReference).get().await().getString("name") }
                else raw.filterIsInstance<String>()
                else      -> emptyList()
            }

            val followers = (data["followers"] as? List<*>)?.size
                ?: data["followers"] as? Int ?: 0
            val following = (data["following"] as? List<*>)?.size
                ?: data["following"] as? Int ?: 0

            val name = (data["name"] as? String)
                ?: (data["user_ref"] as? DocumentReference)
                    ?.get()?.await()?.getString("name") ?: ""

            Profile(
                profilePicture = data["profilePicture"] as? String ?: "",
                description    = data["description"]    as? String ?: "",
                interests      = interests,
                followers      = followers,
                following      = following,
                name           = name
            )
        } catch (e: Exception) {
            Log.e("FirebaseFacade", "fetchUserProfile", e)
            null
        }
    }

    /* ------------------------------------------------------------------ */
    /*  EVENTOS – helper mapper                                            */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun map(raw: List<Map<String, Any>>): List<Event> {
        val fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return raw.map { e ->
            val locRef = e["location_id"] as? DocumentReference
            val locDoc = locRef?.get()?.await()

            val start = (e["start_date"] as? Timestamp)
                ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            val end   = (e["end_date"]   as? Timestamp)
                ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

            Event(
                id          = e["id"]   as? String ?: "",
                name        = e["name"] as? String ?: "",
                city        = e["name"] as? String ?: "",
                description = e["description"] as? String ?: "",
                location    = locDoc?.getString("address") ?: "",
                startDate   = start?.format(fmt).orEmpty(),
                endDate     = end?.format(fmt).orEmpty(),
                category    = (e["category"] as? DocumentReference)
                    ?.get()?.await()?.getString("name") ?: "",
                imageUrl    = e["image"] as? String ?: "",
                cost        = (e["cost"] as? Number)?.toInt() ?: 0,
                attendees   = (e["attendees"] as? List<DocumentReference>)
                    ?.mapNotNull { it.get().await().getString("name") } ?: emptyList(),
                isUniversity= locDoc?.getBoolean("university") ?: false,
                skills      = (e["skills"] as? List<DocumentReference>)
                    ?.mapNotNull { it.get().await().getString("name") } ?: emptyList(),
                creator     = (e["creator_id"] as? DocumentReference)
                    ?.get()?.await()?.getString("name") ?: ""
            )
        }
    }

    /* ------------------------------------------------------------------ */
    /*  EVENTOS – métodos paginados                                        */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeEvents(limit: Long = 5)
            : Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getHomeEventsData(limit)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchHomeEvents", e); Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeEvents(
        limit: Long = 3, lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextHomeEventsData(limit, lastSnapshot)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchNextHomeEvents", e); Pair(emptyList(), null)
    }

    /* -------- MY EVENTS (asistente / creador) -------- */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEvents(limit: Long = 25)
            : Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getMyEventsData(limit)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchMyEvents", e); Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextMyEvents(
        limit: Long = 3, lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextMyEventsData(limit, lastSnapshot)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchNextMyEvents", e); Pair(emptyList(), null)
    }

    /* -------- eventos que YO creé -------- */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchMyEventsCreate(limit: Long = 25)
            : Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getMyEventsCreateData(limit)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchMyEventsCreate", e); Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextMyEventsCreate(
        limit: Long = 3, lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextMyEventsCreateData(limit, lastSnapshot)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchNextMyEventsCreate", e); Pair(emptyList(), null)
    }

    /* -------- recomendados -------- */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchHomeRecommendedEvents(limit: Long = 5)
            : Pair<List<Event>, Set<String>> = try {
        val (raw, ids) = filter.getHomeRecommendedEventsData(limit)
        Pair(map(raw), ids)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchHomeRecommendedEvents", e)
        Pair(emptyList(), emptySet())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextHomeRecommendedEvents(
        limit: Long = 3, offsetIds: Set<String>
    ): List<Event> = try {
        map(filter.getNextHomeRecommendedEventsData(limit, offsetIds))
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchNextHomeRecommendedEvents", e); emptyList()
    }

    /* -------- SEARCH -------- */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchSearchEvents(limit: Long = 5)
            : Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getSearchEventsData(limit)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchSearchEvents", e); Pair(emptyList(), null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchNextSearchEvents(
        limit: Long = 3, lastSnapshot: DocumentSnapshot?
    ): Pair<List<Event>, DocumentSnapshot?> = try {
        val (raw, last) = filter.getNextSearchEventsData(limit, lastSnapshot)
        Pair(map(raw), last)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchNextSearchEvents", e); Pair(emptyList(), null)
    }

    /* ------------------------------------------------------------------ */
    /*  WRAPPERS COMPATIBLES (sin paginación)                             */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O) suspend fun fetchHomeEvents(): List<Event> =
        fetchHomeEvents(5).first
    @RequiresApi(Build.VERSION_CODES.O) suspend fun fetchMyEvents():  List<Event> =
        fetchMyEvents(25).first
    @RequiresApi(Build.VERSION_CODES.O) suspend fun fetchHomeRecommendedEvents(): List<Event> =
        fetchHomeRecommendedEvents(5).first

    /* ------------------------------------------------------------------ */
    /*  LISTADOS SENCILLOS: categorías, skills, locations                 */
    /* ------------------------------------------------------------------ */
    suspend fun fetchCategories(): List<Category> = try {
        filter.getCategoriesData().mapNotNull {
            (it["name"] as? String)?.let(::Category)
        }
    } catch (e: Exception) { Log.e("FirebaseFacade", "fetchCategories", e); emptyList() }

    suspend fun fetchSkills(): List<String> = try {
        filter.getSkillsData().mapNotNull { it["name"] as? String }
    } catch (e: Exception) { Log.e("FirebaseFacade", "fetchSkills", e); emptyList() }

    suspend fun fetchLocations(): List<String> = try {
        filter.getLocationsData()
            .mapNotNull { it["city"] as? String }
            .toSet()      // únicos
            .toList()
    } catch (e: Exception) { Log.e("FirebaseFacade", "fetchLocations", e); emptyList() }

    /* ------------------------------------------------------------------ */
    /*  REGISTRATION DETAIL                                               */
    /* ------------------------------------------------------------------ */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchRegistrationData(eventID: String): Event = try {
        map(listOf(filter.getRegistrationData(eventID))).first().copy(id = eventID)
    } catch (e: Exception) {
        Log.e("FirebaseFacade", "fetchRegistrationData", e); throw e
    }
}

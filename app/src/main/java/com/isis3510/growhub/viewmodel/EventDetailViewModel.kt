package com.isis3510.growhub.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.Location
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.database.AppLocalDatabase

@RequiresApi(Build.VERSION_CODES.O)
class EventDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val eventRepository = EventRepository(AppLocalDatabase.getDatabase(application)) // Inicialización del repositorio
    
    /* ---------- estado expuesto a la UI ---------- */
    val event   = mutableStateOf<Event?>(null)
    val loading = mutableStateOf(true)

    private val db = FirebaseFirestore.getInstance()

    /**  Llama esto desde la vista para traer (o refrescar) el evento  */
    fun loadEvent(name: String, inPreview: Boolean = false) {
        if (!loading.value) loading.value = true

        viewModelScope.launch {
            /* --------- Modo preview --------- */
            if (inPreview) {
                event.value = dummyEvent()          
                loading.value = false
                return@launch
            }

            /* --------- Intentar cargar evento desde la base de datos local ---------- */
            val localEvent = eventRepository.getEventById(name)  // Asegúrate de usar el método adecuado en el repositorio
            if (localEvent != null) {
                event.value = localEvent  // Si se encuentra en la base de datos local, lo usamos directamente
                loading.value = false
                return@launch
            }

            /* --------- fetch en IO ---------- */
            val doc = withContext(Dispatchers.IO) {
                db.collection("events")
                    .whereEqualTo("name", name)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
            } ?: run {
                loading.value = false
                return@launch
            }

            /* --- varias corrutinas en paralelo (Input/Output-10) --- */
            val fullEvent = withContext(Dispatchers.IO) {
                coroutineScope {
                    suspend fun DocumentReference?.string(field: String): String =
                        this?.get()?.await()?.getString(field) ?: "Unknown"

                    val categoryD  = async { doc.getDocumentReference("category").string("name") }
                    val locationD  = async {
                        val locRef = doc.getDocumentReference("location_id")
                        val locSnap = locRef?.get()?.await()
                        if (locSnap != null && locSnap.exists()) {
                            Location(
                                address    = locSnap.getString("address") ?: "Unknown",
                                city       = locSnap.getString("city") ?: "Unknown",
                                latitude   = locSnap.getDouble("latitude") ?: 0.0,
                                longitude  = locSnap.getDouble("longitude") ?: 0.0,
                                university = locSnap.getBoolean("university") ?: false
                            )
                        } else Location("Unknown","Unknown","Unknown",0.0,0.0,false)
                    }
                    val attendeesD = async {
                        (doc["attendees"] as? List<*>)?.mapNotNull {
                            (it as? DocumentReference)?.string("name")
                        } ?: emptyList()
                    }
                    val skillsD = async {
                        (doc["skills"] as? List<*>)?.mapNotNull {
                            (it as? DocumentReference)?.string("name")
                        } ?: emptyList()
                    }

                    /* --- construye el objeto Event ---- */
                    Event(
                        name        = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        location    = locationD.await(),
                        startDate   = doc.getTimestamp("start_date")?.toDate()
                            ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                        endDate     = doc.getTimestamp("end_date")?.toDate()
                            ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                        category    = categoryD.await(),
                        imageUrl    = doc.getString("image") ?: "",
                        cost        = doc.getDouble("cost")?.toInt() ?: 0,
                        attendees   = attendeesD.await(),
                        skills      = skillsD.await(),
                        creator     = ""
                    )
                }
            }

            /* ------------ vuelve al Main (UI) ------------- */
            eventRepository.storeEvents(listOf(fullEvent))
            event.value   = fullEvent          // Input/Output + Main-10
            loading.value = false
        }
    }

    /* --- dato de prueba para @Preview --- */
    private fun dummyEvent() = Event(
        name = "IA Prompt Engineering",
        description = "En un mundo donde la inteligencia artificial ...",
        location = Location("Cra. 1 #18a-12", "Bogotá", "Edificio ML",4.65, -74.05, false),
        startDate = "24 Nov 2025",
        endDate   = "24 Nov 2025",
        category  = "IA Engineers",
        imageUrl  = "https://placehold.co/600x400/png",
        cost      = 0,
        attendees = listOf("Miguel Durán"),
        skills    = listOf("Programming"),
        creator   = ""
    )
}

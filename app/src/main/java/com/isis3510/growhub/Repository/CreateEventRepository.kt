package com.isis3510.growhub.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository que maneja la creación de eventos en Firestore.
 * Aplica el patrón Repository (en MVVM) para separar la lógica de datos de la capa de UI.
 */
class CreateEventRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Crea un nuevo documento en la colección "events" con los campos requeridos.
     * Retorna true si es exitoso, de lo contrario lanza excepción.
     */
    suspend fun createEvent(
        name: String,
        cost: Double,
        category: String,
        description: String,
        startDate: Timestamp,
        endDate: String,
        locationId: String,
        imageUrl: String?,
        address: String,
        details: String
    ): Boolean {
        // Si no se provee imagen, aquí podrías poner una por defecto:
        val finalImageUrl = imageUrl ?: "https://via.placeholder.com/600x300.png?text=Default+Event"

        // Por ahora, iniciamos la lista de usuarios registrada vacía
        val emptyUsersList = arrayListOf<String>() // O arrayListOf<DocumentReference>() si fuera con refs

        // Estructura de los datos que se guardarán en Firestore
        val eventData = hashMapOf(
            "name" to name,
            "cost" to cost,
            "category" to category,
            "description" to description,
            "start_date" to startDate,        // Timestamp
            "end_date" to endDate,           // String según lo pedido
            "location_id" to locationId,     // String con formato "/locations/..."
            "image" to finalImageUrl,        // string
            "users_registered" to emptyUsersList // lista de referencias (ahora vacía)
        )

        // Inserta el documento en la colección "events"
        firestore.collection("events")
            .add(eventData) // add() crea un nuevo documento con ID automático
            .await() // Esperamos la conclusión con corutinas

        return true
    }

}

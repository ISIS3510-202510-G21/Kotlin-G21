package com.isis3510.growhub.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CreateEventRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Ahora se agregan city, isUniversity y skillIds para almacenarlos en Firestore
     */
    suspend fun createEvent(
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
        city: String,                         // NUEVO
        isUniversity: Boolean,               // NUEVO
        skillIds: List<String>               // NUEVO - IDs de los skills seleccionados
    ): Boolean {
        // Primero buscamos la categoría correspondiente en Firestore
        val categoryQuerySnapshot = firestore.collection("categories")
            .whereEqualTo("name", category)
            .get()
            .await()

        // Si no encuentra ningún documento con ese "name", retornamos false
        if (categoryQuerySnapshot.isEmpty) {
            return false
        }

        // Tomamos la primera coincidencia como la categoría a referenciar
        val categoryRef = categoryQuerySnapshot.documents[0].reference

        // Datos de la ubicación (ya NO se fija en Bogotá ni en university=true)
        val locationData = hashMapOf(
            "address" to address,
            "city" to city,                       // NUEVO
            "details" to details,
            "university" to isUniversity          // NUEVO
        )

        // Creamos la ubicación y obtenemos su referencia
        val locationRef: DocumentReference = firestore.collection("locations")
            .add(locationData)
            .await()

        // Imagen por defecto si no se recibe una
        val finalImageUrl = imageUrl
            ?: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQKF_YlFFlKS6AQ8no0Qs_xM6AkjvwFwP61og&s"

        // Lista de usuarios vacía de inicio
        val emptyUsersList = arrayListOf<String>()

        // NUEVO: Convertimos cada skillId a una DocumentReference de la colección "skills"
        val skillRefs = skillIds.map { skillId ->
            firestore.collection("skills").document(skillId)
        }

        // Datos del evento
        val eventData = hashMapOf(
            "name" to name,
            "cost" to cost,
            "category" to categoryRef,       // Se guarda como referencia
            "description" to description,
            "start_date" to startDate,
            "end_date" to endDate,
            "location_id" to locationRef,    // Referencia al documento de location
            "image" to finalImageUrl,
            "users_registered" to emptyUsersList,
            "skills" to skillRefs            // NUEVO: lista de referencias a "skills"
        )

        // Guardamos el evento en la colección "events"
        firestore.collection("events")
            .add(eventData)
            .await()

        return true
    }
}

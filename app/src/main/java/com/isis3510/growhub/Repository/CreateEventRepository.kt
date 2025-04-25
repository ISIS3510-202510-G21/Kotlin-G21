package com.isis3510.growhub.Repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CreateEventRepository {

    private val firestore = FirebaseFirestore.getInstance()

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
        details: String
    ): Boolean {
        // Primero buscamos la categoría correspondiente en Firestore
        val categoryQuerySnapshot = firestore.collection("categories")
            .whereEqualTo("name", category)
            .get()
            .await()

        // Si no encuentra ningún documento con ese "name", puedes decidir cómo manejarlo
        // En este caso, simplemente retornamos false indicando que no se pudo crear el evento
        if (categoryQuerySnapshot.isEmpty) {
            return false
        }

        // Tomamos la primera coincidencia como la categoría a referenciar
        val categoryRef = categoryQuerySnapshot.documents[0].reference

        // Datos de la ubicación
        val locationData = hashMapOf(
            "address" to address,
            "city" to "Bogotá",
            "details" to details,
            "university" to true
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

        // Datos del evento
        val eventData = hashMapOf(
            "name" to name,
            "cost" to cost,
            // Se guarda como referencia el documento encontrado en categories
            "category" to categoryRef,
            "description" to description,
            "start_date" to startDate,
            "end_date" to endDate,
            "location_id" to locationRef,  // Referencia al documento de location
            "image" to finalImageUrl,
            "users_registered" to emptyUsersList
        )

        // Guardamos el evento en la colección "events"
        firestore.collection("events")
            .add(eventData)
            .await()

        return true
    }
}

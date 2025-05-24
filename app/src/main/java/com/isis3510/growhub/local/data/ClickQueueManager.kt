package com.isis3510.growhub.local.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.isis3510.growhub.utils.MapClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manejador de cola de clicks pendientes para soporte offline
 */
class ClickQueueManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "click_queue_prefs", Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val queueKey = "pending_clicks"
    private val maxQueueSize = 100 // Limitar tamaño de la cola para evitar problemas de memoria

    /**
     * Agregar un click a la cola de pendientes
     */
    suspend fun enqueueClick(click: MapClick): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentQueue = getPendingClicks()

            // Verificar si la cola está llena
            if (currentQueue.size >= maxQueueSize) {
                // Descartar el click más antiguo si superamos el límite
                if (currentQueue.isNotEmpty()) {
                    currentQueue.removeAt(0)
                }
                Log.w("ClickQueue", "Cola llena, descartando click más antiguo")
            }

            // Agregar nuevo click
            currentQueue.add(click)

            // Guardar la cola actualizada
            val json = gson.toJson(currentQueue)
            preferences.edit().putString(queueKey, json).apply()

            Log.d("ClickQueue", "Click añadido a la cola: ${click.clickType}. Total en cola: ${currentQueue.size}")
            return@withContext true
        } catch (e: Exception) {
            Log.e("ClickQueue", "Error al encolar click", e)
            return@withContext false
        }
    }

    /**
     * Obtener todos los clicks pendientes
     */
    suspend fun getPendingClicks(): MutableList<MapClick> = withContext(Dispatchers.IO) {
        val json = preferences.getString(queueKey, null) ?: return@withContext mutableListOf()
        try {
            val type = object : TypeToken<MutableList<MapClick>>() {}.type
            return@withContext gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            Log.e("ClickQueue", "Error al leer cola de clicks", e)
            // En caso de error, limpiar la cola corrupta
            preferences.edit().remove(queueKey).apply()
            return@withContext mutableListOf()
        }
    }

    /**
     * Eliminar clicks después de sincronizarlos
     */
    suspend fun removeProcessedClicks(processedClicks: List<MapClick>) = withContext(Dispatchers.IO) {
        try {
            val currentQueue = getPendingClicks()
            // Filtrar los clicks ya procesados (comprobando timestamp y tipo)
            val updatedQueue = currentQueue.filterNot { pending ->
                processedClicks.any { processed ->
                    processed.userId == pending.userId &&
                            processed.clickType == pending.clickType &&
                            processed.timestamp == pending.timestamp
                }
            }.toMutableList()

            // Guardar la cola actualizada
            val json = gson.toJson(updatedQueue)
            preferences.edit().putString(queueKey, json).apply()

            Log.d("ClickQueue", "${processedClicks.size} clicks eliminados de la cola. Quedan: ${updatedQueue.size}")
        } catch (e: Exception) {
            Log.e("ClickQueue", "Error al actualizar cola después de procesar", e)
        }
    }

    /**
     * Limpiar toda la cola (útil para logout)
     */
    suspend fun clearQueue() = withContext(Dispatchers.IO) {
        preferences.edit().remove(queueKey).apply()
        Log.d("ClickQueue", "Cola de clicks limpiada")
    }
}
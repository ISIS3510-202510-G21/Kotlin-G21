package com.isis3510.growhub.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.viewmodel.SimpleLocationUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

sealed interface SimpleLocationUiState {
    object Idle : SimpleLocationUiState
    object Loading : SimpleLocationUiState
    data class Success(val rawResponse: String) : SimpleLocationUiState
    data class Error(val message: String) : SimpleLocationUiState
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

    private val _locationState = MutableStateFlow<SimpleLocationUiState>(SimpleLocationUiState.Idle)
    val locationState: StateFlow<SimpleLocationUiState> = _locationState.asStateFlow()

    private val apiUrl = "http://ip-api.com/json/"

    init {
        fetchLocationFromIpApi()
    }

    fun fetchLocationFromIpApi() {
        if (_locationState.value is SimpleLocationUiState.Loading) return
        _locationState.update { SimpleLocationUiState.Loading }

        viewModelScope.launch {
            val result: SimpleLocationUiState = withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(apiUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 8000
                        readTimeout = 8000
                        connect()
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                        // → parseo JSON:
                        val json = JSONObject(response)
                        val lat = json.optDouble("lat", Double.NaN)
                        val lon = json.optDouble("lon", Double.NaN)

                        if (!lat.isNaN() && !lon.isNaN()) {
                            // → guardo en SharedPreferences:
                            prefs.edit()
                                .putString("user_latitude", lat.toString())
                                .putString("user_longitude", lon.toString())
                                .apply()
                            Log.d("LocationViewModel", "Saved coords: $lat, $lon")
                        } else {
                            Log.w("LocationViewModel", "No se obtuvieron coords válidas")
                        }

                        SimpleLocationUiState.Success(response)
                    } else {
                        val errorMsg = connection.errorStream
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: "No hay detalles de error"
                        SimpleLocationUiState.Error("HTTP ${connection.responseCode}: $errorMsg")
                    }
                } catch (e: Exception) {
                    SimpleLocationUiState.Error(e.localizedMessage ?: "Error de red desconocido")
                } finally {
                    connection?.disconnect()
                }
            }
            _locationState.value = result
        }
    }

    /** Para recuperar las coordenadas más adelante: */
    fun getLastKnownLatLng(): Pair<Double?, Double?> {
        val lat = prefs.getString("user_latitude", null)?.toDoubleOrNull()
        val lon = prefs.getString("user_longitude", null)?.toDoubleOrNull()
        return Pair(lat, lon)
    }
}

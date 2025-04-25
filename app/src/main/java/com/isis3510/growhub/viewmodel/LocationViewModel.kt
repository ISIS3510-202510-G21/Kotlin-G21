package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// --- Simplified UI State ---
// Success now holds the raw JSON String
sealed interface SimpleLocationUiState {
    object Loading : SimpleLocationUiState
    data class Success(val rawResponse: String) : SimpleLocationUiState // Holds the raw JSON string
    data class Error(val message: String) : SimpleLocationUiState
    object Idle : SimpleLocationUiState
}

class LocationViewModel : ViewModel() {

    private val _locationState = MutableStateFlow<SimpleLocationUiState>(SimpleLocationUiState.Idle)
    val locationState: StateFlow<SimpleLocationUiState> = _locationState.asStateFlow()

    // API Endpoint URL
    private val apiUrl = "http://ip-api.com/json/"

    init {
        fetchLocationFromIpApi()
    }

    fun fetchLocationFromIpApi() {
        // Prevent fetching if already loading
        if (_locationState.value is SimpleLocationUiState.Loading) return

        _locationState.update { SimpleLocationUiState.Loading }

        viewModelScope.launch {
            // Perform network operation on the IO dispatcher
            val result: SimpleLocationUiState = withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(apiUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 8000 // 8 seconds
                    connection.readTimeout = 8000 // 8 seconds
                    connection.connect()

                    val responseCode = connection.responseCode

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the successful response body
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = reader.use { it.readText() } // Reads entire stream to String
                        SimpleLocationUiState.Success(response)
                    } else {
                        // Read the error body if available
                        val errorStream = connection.errorStream
                        val errorMessage = try {
                            errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details available."
                        } catch (e: Exception) {
                            "Failed to read error stream."
                        }
                        SimpleLocationUiState.Error("HTTP Error: $responseCode. $errorMessage")
                    }
                } catch (e: Exception) {
                    // Catch network errors (UnknownHostException, SocketTimeoutException, etc.)
                    // Or MalformedURLException, SecurityException, etc.
                    SimpleLocationUiState.Error(e.message ?: "An unknown network error occurred.")
                } finally {
                    // Always disconnect
                    connection?.disconnect()
                }
            }
            // Update the state flow back on the main thread (viewModelScope handles this)
            _locationState.value = result
        }
    }
}
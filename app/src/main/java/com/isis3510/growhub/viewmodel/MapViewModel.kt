package com.isis3510.growhub.viewmodel

import android.annotation.SuppressLint // Necesario para ConnectivityManager y FusedLocation
import android.app.Application // *** Importar Application ***
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager // *** Importar ConnectivityManager ***
import android.net.NetworkCapabilities // *** Importar NetworkCapabilities ***
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel // *** Cambiar a AndroidViewModel ***
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.isis3510.growhub.local.data.GlobalData
// import com.isis3510.growhub.model.facade.FirebaseServicesFacade // No se usa si es solo GlobalData
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.MarkerData
import kotlinx.coroutines.* // Importar CoroutineScope, Job, delay, etc.

@RequiresApi(Build.VERSION_CODES.O)
// *** Heredar de AndroidViewModel ***
class MapViewModel(application: Application) : AndroidViewModel(application) {

    // --- Estados ---
    val nearbyEvents = mutableStateListOf<Event>()

    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    private val _eventMarkers = mutableStateOf<List<MarkerData>>(emptyList())
    val eventMarkers: State<List<MarkerData>> = _eventMarkers

    // *** Nuevos Estados para Conectividad y Carga ***
    private val _isLoading = mutableStateOf(true) // Empieza cargando
    val isLoading: State<Boolean> = _isLoading
    private val _errorMessage = mutableStateOf<String?>(null)

    // --- Internals ---
    private var pollingJob: Job? = null // Job para la consulta periódica
    private val pollingIntervalMillis = 5000L // Intervalo de consulta (e.g., 5 segundos)
    private val logTag = "MapViewModelConn"

    // --- Lógica de Ubicación (sin cambios respecto a tu versión) ---
    @SuppressLint("MissingPermission") // Permiso chequeado en la View
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                        Log.d(logTag, "Ubicación obtenida: $userLatLng")
                    } ?: run {
                        Log.w(logTag, "No last known location available.")
                        // Considerar poner _userLocation.value = null aquí explícitamente
                    }
                }.addOnFailureListener { e ->
                    Log.e(logTag, "Error FusedLocationProvider.lastLocation", e)
                    // Podrías setear un error aquí si la ubicación es crítica
                }
            } catch (e: SecurityException) {
                Log.e(logTag, "Location access permission error: ${e.localizedMessage}")
            }
        } else {
            Log.w(logTag, "fetchUserLocation llamado sin permisos concedidos.")
        }
    }

    // --- Lógica de Carga Inicial y Consulta Periódica ---
    init {
        loadNearbyEventsToList() // Inicia la carga al crear el ViewModel
    }

    private fun loadNearbyEventsToList() {
        viewModelScope.launch { // Lanza en el scope del ViewModel
            _isLoading.value = true // Indica inicio de carga
            _errorMessage.value = null // Limpia errores previos
            var dataLoaded = false // Flag para saber si se cargó algo

            try {
                val eventsFromGlobal = GlobalData.nearbyEvents
                Log.d(logTag, "GlobalData.nearbyEvents tiene ${eventsFromGlobal.size} elementos.")

                if (eventsFromGlobal.isNotEmpty()) {
                    // ======= MULTITHREADING ADDED =======:
                    // Procesamiento de marcadores en hilo de fondo
                    val markers = processMarkersInBackground(eventsFromGlobal)

                    // Actualiza estado en hilo principal
                    withContext(Dispatchers.Main) {
                        nearbyEvents.clear()
                        nearbyEvents.addAll(eventsFromGlobal)
                        _eventMarkers.value = markers
                        dataLoaded = true
                    }
                } else {
                    if (!isNetworkAvailable()) {
                        // Sin datos y sin conexión
                        Log.e(logTag, "No connection and no data in GlobalData.")
                        withContext(Dispatchers.Main) {
                            _errorMessage.value = "No Connection. Please try again later"
                        }
                    } else {
                        Log.i(logTag, "GlobalData empty, periodic polling started.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error getting Event Data."
                }
            } finally {
                // Asegura que isLoading se ponga a false y se inicie el polling si es necesario
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    if (!dataLoaded && nearbyEvents.isEmpty()) { // Inicia polling solo si realmente no se cargó nada
                        startPollingForData()
                    }
                }
            }
        }
    }

    // ======= MULTITHREADING ADDED =======:
    // Helper para procesar marcadores en Dispatchers.Default
    private suspend fun processMarkersInBackground(events: List<Event>): List<MarkerData> {
        return withContext(Dispatchers.Default) {
            events.mapNotNull { event ->
                runCatching {
                    // Asumiendo que MarkerData tiene id y los métodos de location existen
                    MarkerData(
                        id = event.id,
                        position = event.location.getCoordinates(),
                        title = event.name,
                        snippet = event.location.getInfo()
                    )
                }.onFailure { e ->
                    Log.e(logTag, "Error creando MarkerData para evento ${event.id}", e)
                }.getOrNull()
            }
        }
    }

    // Inicia la consulta periódica si no hay datos
    private fun startPollingForData() {
        // Cancela cualquier polling anterior
        stopPolling()
        // Inicia solo si la lista sigue vacía
        if (nearbyEvents.isEmpty()) {
            Log.i(logTag, "Starting polling every ${pollingIntervalMillis}ms...")
            pollingJob = viewModelScope.launch(Dispatchers.IO) { // Ejecuta el bucle en IO
                while (isActive) { // Bucle mientras la corrutina esté activa
                    try {
                        // Verify connection before polling
                        if (!isNetworkAvailable()) {
                            Log.d(logTag, "Polling: No connection, waiting...")
                            delay(pollingIntervalMillis * 2) // Espera más si no hay conexión
                            continue // Salta esta iteración
                        }

                        val currentGlobalEvents = GlobalData.nearbyEvents
                        if (currentGlobalEvents.isNotEmpty()) {
                            Log.i(logTag, "Polling: ¡Data found in GlobalData! (${currentGlobalEvents.size} events)")
                            // ======= MULTITHREADING ADDED =======:
                            val markers = processMarkersInBackground(currentGlobalEvents)
                            // Actualiza la UI en el hilo principal
                            withContext(Dispatchers.Main) {
                                nearbyEvents.clear()
                                nearbyEvents.addAll(currentGlobalEvents)
                                _eventMarkers.value = markers
                                _errorMessage.value = null
                                _isLoading.value = false
                                stopPolling() // Detiene el polling una vez que encuentra datos
                            }
                        } else {
                            Log.d(logTag, "Polling: GlobalData still empty.")
                        }
                    } catch (e: Exception) {
                        Log.e(logTag, "Polling: Error during polling", e)
                    }
                    delay(pollingIntervalMillis) // Espera antes de la siguiente consulta
                }
            }
        } else {
            Log.d(logTag, "No polling because there is already data.")
        }
    }

    // Detiene la consulta periódica
    private fun stopPolling() {
        if (pollingJob?.isActive == true) {
            Log.i(logTag, "Stopping polling...")
            pollingJob?.cancel()
        }
        pollingJob = null
    }

    // --- Helper de Conectividad ---
    @SuppressLint("MissingPermission") // El permiso ACCESS_NETWORK_STATE es normal, no peligroso
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return false // Retorna false si no se puede obtener el servicio

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            // Deprecated para API < 23, pero como fallback
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling() // Asegura detener el polling cuando el ViewModel se destruye
        Log.d(logTag, "ViewModel cleared, polling stopped.")
    }
}

package com.isis3510.growhub.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.local.data.GlobalData
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.MarkerData
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.utils.MapClick
import com.isis3510.growhub.utils.SearchEventClick
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class MapViewModel(application: Application) : AndroidViewModel(application) {
    // Estados de datos
    val nearbyEvents = mutableStateListOf<Event>()
    private val _eventMarkers = mutableStateOf<List<MarkerData>>(emptyList())
    val eventMarkers: State<List<MarkerData>> = _eventMarkers
    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Conectividad
    private val connectivityViewModel = ConnectivityViewModel(application)
    private val _isOffline = mutableStateOf(false)
    val isOffline: State<Boolean> = _isOffline

    // Polling
    private var pollingJob: Job? = null
    private val pollingIntervalMillis = 5000L
    private val logTag = "MapViewModelConn"

    // To keep track of clicks
    private val clickStats = mutableMapOf<String, Int>()

    init {
        // Observar cambios de red
        viewModelScope.launch {
            connectivityViewModel.networkStatus.collectLatest { status ->
                _isOffline.value = status == ConnectionStatus.Unavailable || status == ConnectionStatus.Lost
                Log.d(logTag, "Network status changed: $status, isOffline=${_isOffline.value}")
                // Opcional: recargar datos al reconectar
                if (!_isOffline.value) {
                    loadNearbyEventsToList()
                }
            }
        }
        // Carga inicial
        loadNearbyEventsToList()
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        val fine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                    Log.d(logTag, "Location: ${_userLocation.value}")
                }
            }
        }
    }

    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    private fun loadNearbyEventsToList() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            var dataLoaded = false

            try {
                val globalList = GlobalData.nearbyEvents
                Log.d(logTag, "GlobalData size: ${globalList.size}")

                if (globalList.isNotEmpty()) {
                    val markers = processMarkers(globalList)
                    withContext(Dispatchers.Main) {
                        nearbyEvents.clear()
                        nearbyEvents.addAll(globalList)
                        _eventMarkers.value = markers
                        dataLoaded = true
                    }
                } else {
                    if (_isOffline.value) {
                        Log.e(logTag, "Offline & no data in cache.")
                        withContext(Dispatchers.Main) {
                            _errorMessage.value = "No Connection. Try again later."
                        }
                    } else {
                        Log.i(logTag, "No data, starting polling...")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error loading events."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    if (!dataLoaded && nearbyEvents.isEmpty()) startPollingForData()
                }
            }
        }
    }

    private suspend fun processMarkers(events: List<Event>): List<MarkerData> =
        withContext(Dispatchers.Default) {
            events.mapNotNull { ev ->
                runCatching {
                    MarkerData(
                        id = ev.name,
                        position = ev.location.getCoordinates(),
                        title = ev.name,
                        snippet = ev.location.getInfo()
                    )
                }.getOrNull()
            }
        }

    private fun startPollingForData() {
        stopPolling()
        if (nearbyEvents.isEmpty()) {
            pollingJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    if (_isOffline.value) {
                        Log.d(logTag, "Polling: still offline.")
                        delay(pollingIntervalMillis * 2)
                        continue
                    }
                    val list = GlobalData.nearbyEvents
                    if (list.isNotEmpty()) {
                        val markers = processMarkers(list)
                        withContext(Dispatchers.Main) {
                            nearbyEvents.clear()
                            nearbyEvents.addAll(list)
                            _eventMarkers.value = markers
                            _errorMessage.value = null
                            _isLoading.value = false
                        }
                        break
                    }
                    delay(pollingIntervalMillis)
                }
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun refreshNearbyEvents() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadNearbyEventsToList()
            _isRefreshing.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        val cm = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager? ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            return cm.activeNetworkInfo?.isConnected == true
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        Log.d(logTag, "Cleared, polling stopped.")
    }

    /**
     * Log button click for analytics with proper error handling and offline support
     */
    fun logClick(clickType: String) {
        viewModelScope.launch {
            try {
                // Incrementar conteo local para su uso en la UI o para sincronización posterior
                val currentCount = clickStats.getOrDefault(clickType, 0)
                clickStats[clickType] = currentCount + 1

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.w("ClickLog", "No user authenticated, click not sent to Firebase")
                    return@launch
                }

                // Verificar conectividad antes de intentar enviar
                if (_isOffline.value) {
                    Log.w("ClickLog", "Device offline, click will be sent later")
                    // Aquí podría implementar una cola de clicks pendientes

                    return@launch
                }

                val clickEvent = MapClick(
                    userId = currentUser.uid,
                    clickType = clickType
                )
                Log.d("UserID", currentUser.uid)
                Log.d("ClickLog", "Enviando click a Firebase: $clickEvent")

                FirebaseFirestore.getInstance()
                    .collection("map_clicks")
                    .add(clickEvent)
                    .await()

            } catch (e: Exception) {
                Log.e("ClickLog", "Excepción al procesar click: $clickType", e)
            }
        }
    }
}

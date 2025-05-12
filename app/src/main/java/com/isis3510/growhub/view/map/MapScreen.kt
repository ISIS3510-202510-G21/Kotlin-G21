package com.isis3510.growhub.view.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.viewmodel.ConnectivityViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapView(
    mapViewModel: MapViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    var isOffline = mapViewModel.isOffline

    // Estado para saber si el permiso está concedido
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Estado para saber si el mapa debe mostrarse (después de que el diálogo de permiso se cierre)
    var showMap by remember { mutableStateOf(hasPermission) } // Mostrar mapa si ya tiene permiso

    // *** Estado para guardar el ID del evento seleccionado ***
    var selectedEventId by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        showMap = true
        if (isGranted) {
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        }
    }

    LaunchedEffect(Unit) {
        // Verifica el permiso al inicio
        val initialPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        hasPermission = initialPermission
        showMap = initialPermission // Mostrar mapa inmediatamente si ya tiene permiso

        if (initialPermission) {
            // Si ya tiene permiso, obtiene la ubicación
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        } else {
            // Si no tiene permiso, lanza la solicitud
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            // showMap se mantendrá false hasta que onResult se ejecute
        }
    }

    Scaffold(
        topBar = { MapTopBar(onNavigateBack, isOffline) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Renderiza MapContent SI showMap es true
            if (showMap) {
                // Pasa el ID seleccionado a MapContent
                MapContent(mapViewModel, cameraPositionState, hasPermission, selectedEventId, isOffline)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Misma altura que el mapa
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Pasa el ID seleccionado y el callback a EventsList
            val isRefreshing by mapViewModel.isRefreshing
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    if (!isOffline) {
                        mapViewModel.refreshNearbyEvents()
                    } else {
                        Toast.makeText(context, "Nearby events cannot be refreshed while you are offline", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                EventsList(
                    events = mapViewModel.nearbyEvents,
                    selectedEventId = selectedEventId,
                    onEventClick = { eventId ->
                        selectedEventId = if (selectedEventId == eventId) null else eventId
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 50.dp), // Considerar usar Scaffold's bottomBar
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar(navController = navController)
        }
    }
}

@Composable
fun MapTopBar(
    onNavigateBack: () -> Unit = {},
    isOffline: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Events close to you",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isOffline) {
            Text(
                text = "Offline",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Red, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapContent(
    viewModel: MapViewModel,
    cameraPositionState: CameraPositionState,
    hasPermission: Boolean,
    selectedEventId: String?,
    isOffline: Boolean
) {
    val userLocation by viewModel.userLocation
    val eventMarkersData by viewModel.eventMarkers
    val coroutineScope = rememberCoroutineScope()

    // Mapa para guardar los MarkerStates y poder mostrar/ocultar info windows
    val markerStates = remember { mutableMapOf<String, MarkerState>() }
    // Guarda el ID del marcador cuya info window está visible (controlado por LaunchedEffect)
    var currentInfoWindowId by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(userLocation) {
        userLocation?.let {
            if (cameraPositionState.position.target != it) {
                coroutineScope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 11f))
                }
            }
        }
    }

    // Efecto para mostrar/ocultar InfoWindow cuando selectedEventId cambia
    LaunchedEffect(selectedEventId) {
        // Oculta la ventana anterior si existe y es diferente a la nueva
        if (currentInfoWindowId != null && currentInfoWindowId != selectedEventId) {
            markerStates[currentInfoWindowId]?.hideInfoWindow()
        }
        // Muestra la nueva ventana si hay un ID seleccionado
        if (selectedEventId != null) {
            val markerData = eventMarkersData.find { it.id == selectedEventId } // Asume que MarkerData tiene id
            if (markerData != null) {
                markerStates[selectedEventId]?.showInfoWindow() // *** Muestra el snippet ***
                currentInfoWindowId = selectedEventId // Actualiza el ID visible
            } else {
                currentInfoWindowId = null // No se encontró el marcador
            }
        } else {
            // Si selectedEventId es null, oculta la ventana actual si hay una
            if (currentInfoWindowId != null) {
                markerStates[currentInfoWindowId]?.hideInfoWindow()
            }
            currentInfoWindowId = null // Ninguna ventana visible
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(myLocationButtonEnabled = true,
                    zoomControlsEnabled = !(isOffline),
                    scrollGesturesEnabled = !(isOffline),
                    zoomGesturesEnabled = !(isOffline)
                ),
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            // Marcador de Usuario (Violeta) - Condicional
            if (hasPermission) {
                userLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = loc),
                        title = "Your Current Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET) // Color Violeta
                    )
                }
            }

            // Marcadores de Eventos
            eventMarkersData.forEach { markerData -> // Itera sobre MarkerData
                // Recuerda y guarda el MarkerState
                val markerState = rememberMarkerState(position = markerData.position)
                // Asocia el ID del evento
                markerStates[markerData.id] = markerState

                Marker(
                    state = markerState, // Usa el state recordado
                    title = markerData.title,
                    snippet = markerData.snippet,
                    icon = if (markerData.id == selectedEventId) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) // Color Blue
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) // Color Red
                    }
                )
            }
        }
    }
}



@Composable
fun EventsList(
    events: List<Event>,
    selectedEventId: String?, // *** Recibe el ID seleccionado ***
    onEventClick: (String) -> Unit // *** Recibe el callback ***
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Aumentar padding inferior
    ) {
        items(events, key = { event -> event.name }) { event -> // *** Usa event.id como key ***
            val isSelected = event.name == selectedEventId // *** Determina si está seleccionado ***
            EventCard(
                event = event,
                isSelected = isSelected, // *** Pasa el estado de selección ***
                onClick = { onEventClick(event.name) } // *** Llama al callback al hacer clic ***
            )
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        // Cambia el color del contenedor basado en isSelected
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick) // Hacer la tarjeta clickeable
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(event.imageUrl),
                contentDescription = event.name,
                modifier = Modifier
                    .size(80.dp)
                    // Mantenemos el fondo original, no lo cambiamos con la selección
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.startDate,
                    fontSize = 16.sp,
                    // *** Cambia color de texto si está seleccionado ***
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    // *** Cambia color de texto si está seleccionado ***
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant // Ajustar color base
                )
                Text(
                    text = event.location.getInfo(), // Asume que getInfo existe
                    fontSize = 14.sp,
                    // *** Cambia color de texto si está seleccionado ***
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // Color más sutil
                )
            }
        }
    }
}
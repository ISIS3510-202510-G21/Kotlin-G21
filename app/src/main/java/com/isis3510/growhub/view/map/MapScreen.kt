package com.isis3510.growhub.view.map

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.MapUiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.viewmodel.MapViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.isis3510.growhub.utils.AdvancedMarkersMapContent
import com.isis3510.growhub.viewmodel.NearbyEventsViewModel

@Composable
fun MapView(
    mapViewModel: MapViewModel,
    mappedEventsViewModel: NearbyEventsViewModel,
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    // ============================================================
    // VARIABLES AND OTHER NECESSARY DECLARATIONS
    // ============================================================
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current

    // Observe user location from MapViewModel
    val userLocation by mapViewModel.userLocation

    // Observe events to map from NearbyEventsViewModel
    val mapEvents by mappedEventsViewModel.mapEvents.collectAsState()
    val isLoading by mappedEventsViewModel.isLoading.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val existingCoordinates = mutableSetOf<Pair<Double, Double>>() // Set to track used coordinates


    // ============================================================
    // PERMISSION HANDLING
    // ============================================================
    // Handle location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        }
    }

    // Request the location permission when the composable is launched
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            // Check if the location permission is already granted
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Fetch the user's location and update the camera
                mapViewModel.fetchUserLocation(context, fusedLocationClient)
            }
            else -> {
                // Request the location permission if it has not been granted
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }


    // ============================================================
    // INVOCATION TO FETCH CASCADE
    // ============================================================
    var isCameraMovedToUserLocation by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            mappedEventsViewModel.fetchUserLocation(location.latitude, location.longitude)

            // Move the camera to user's location only once
            if (!isCameraMovedToUserLocation) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f)
                )
                isCameraMovedToUserLocation = true
            }
        }
    }

    // ============================================================
    // FRONTEND DESIGN
    // ============================================================
    Scaffold(
        topBar = { MapTopBar(onNavigateBack) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Show loading indicator while fetching crimes
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                MapContent(mapViewModel, mappedEventsViewModel, mapEvents, cameraPositionState)
            }
            EventsList(mappedEventsViewModel.nearbyEvents)
        }

        Box(modifier = Modifier.fillMaxSize().offset(y = 50.dp), contentAlignment = Alignment.BottomCenter) {
            BottomNavigationBar(navController = navController)
        }
    }
}

@Composable
fun MapTopBar(onNavigateBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xff191d17)
                )
            }
            Text(
                text = "Events close to you",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xff191d17)
            )
        }
    }
}


@Composable
fun MapContent(
        viewModel: MapViewModel,
        eventsVM: NearbyEventsViewModel,
        mapEvents: List<LatLng>,
        cameraPositionState: CameraPositionState
    ) {

    val userLocation by viewModel.userLocation

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true
            )
        ) {
            // Si la ubicación del usuario está disponible, coloca su marker.
            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Your Location",
                    snippet = "This is where you are currently located."
                )
            }

            // Muestra los markers customizados de los eventos.
            AdvancedMarkersMapContent(
                events = eventsVM.nearbyEvents,
                coordinates = mapEvents,
                onEventClick = { marker ->
                    // Acción al hacer click en un marker de evento.
                    false
                }
            )
        }

    }
}

@Composable
fun EventsList(events: List<Event>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(event)
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del evento
            Image(
                painter = rememberAsyncImagePainter(event.imageUrl),
                contentDescription = event.title,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Detalles del evento
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.date, fontSize = 16.sp, color = Color(0xFF5669FF))
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xff191d17)
                )
                Text(text = event.location, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
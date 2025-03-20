package com.isis3510.growhub.view.map

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.viewmodel.MapViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable

@Composable
fun MapView(
    viewModel: MapViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
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
            MapPlaceholder()
            EventsList(viewModel.nearbyEvents)
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
fun MapPlaceholder() {
    var isMapLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Add GoogleMap here
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            onMapLoaded = { isMapLoaded = true }
        )
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

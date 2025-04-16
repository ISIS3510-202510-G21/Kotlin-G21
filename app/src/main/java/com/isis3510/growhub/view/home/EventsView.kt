package com.isis3510.growhub.view.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Import getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.viewmodel.HomeEventsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventsView(
    modifier: Modifier = Modifier,
    eventsViewModel: HomeEventsViewModel,
    onEventClick: (Event) -> Unit
) {
    // Observa las listas y el estado de carga
    val upcomingEvents = eventsViewModel.upcomingEvents
    val nearbyEvents = eventsViewModel.nearbyEvents
    val recommendedEvents = eventsViewModel.recommendedEvents
    val isLoadingUpcoming by eventsViewModel.isLoadingUpcoming
    val isLoadingNearby by eventsViewModel.isLoadingNearby
    val isLoadingRecommended by eventsViewModel.isLoadingRecommended

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Upcoming Events Section ---
        if (isLoadingUpcoming) {
            // Mientras se carga se muestra el placeholder
            EventSectionPlaceholder(title = "Upcoming Events")
        } else {
            // Si no hay datos se muestra el placeholder específico de sección
            if (upcomingEvents.isNotEmpty()) {
                EventSection(
                    title = "Upcoming Events",
                    events = upcomingEvents,
                    onEventClick = onEventClick
                )
            } else {
                EventSectionEmpty(title = "Upcoming Events")
            }
        }

        // --- Nearby Events Section ---
        if (isLoadingNearby) {
            EventSectionPlaceholder(title = "Nearby Events")
        } else {
            if (nearbyEvents.isNotEmpty()) {
                EventSection(
                    title = "Nearby Events",
                    events = nearbyEvents,
                    onEventClick = onEventClick
                )
            } else {
                EventSectionEmpty(title = "Nearby Events")
            }
        }

        // --- Recommended Events Section ---
        if (isLoadingRecommended) {
            EventSectionPlaceholder(title = "You may like")
        } else {
            if (recommendedEvents.isNotEmpty()) {
                EventSection(
                    title = "You may like",
                    events = recommendedEvents,
                    onEventClick = onEventClick,
                )
            } else {
                EventSectionEmpty(title = "You may like")
            }
        }
    }
}


@Composable
fun EventSectionEmpty(
    title: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sección con el encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        // Área centralizada para indicar "No Events Found"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No events found icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Events Found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EventSection(
    title: String,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Horizontal List of Events
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(
                items = events,
                key = { event -> "${event.name}-${event.startDate}" }
            ) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(215.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(model = event.imageUrl),
                contentDescription = "Image for ${event.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(115.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray)
            )

            // Text content section
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxHeight(), // Fills remaining space in Card
                verticalArrangement = Arrangement.SpaceBetween // Pushes details to bottom
            ) {
                // Top part: Event Name
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium, // Adjusted for space maybe
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2, // Allow two lines for name
                    overflow = TextOverflow.Ellipsis
                )

                // Bottom part: Event Details (Date, Location)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.startDate ?: "N/A", // Handle null date
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location, // Assume location is usually present
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun EventSectionPlaceholder(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for Title
            Box(
                modifier = Modifier
                    .height(24.dp) // Approx titleLarge height
                    .fillMaxWidth(0.4f) // Adjust width as needed
                    .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(3) { // Show 3 card placeholders
                EventCardPlaceholder()
            }
        }
    }
}

@Composable
private fun EventCardPlaceholder() {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(215.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation for placeholder
    ) {
        Column {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .height(115.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.4f))
            )
            // Text Content Placeholder
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween // Match real card layout
            ) {
                // Name Placeholder
                Box(
                    modifier = Modifier
                        .height(20.dp) // Approx titleMedium height
                        .fillMaxWidth(0.8f)
                        .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                // Details Placeholder Group
                Column {
                    Box(
                        modifier = Modifier
                            .height(14.dp) // Approx bodySmall height
                            .fillMaxWidth(0.5f)
                            .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(14.dp) // Approx bodySmall height
                            .fillMaxWidth(0.7f)
                            .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
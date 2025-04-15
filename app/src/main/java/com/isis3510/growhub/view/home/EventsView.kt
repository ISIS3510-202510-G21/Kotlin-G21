package com.isis3510.growhub.view.home

import android.os.Build
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onEventClick: (Event) -> Unit,
    onSeeAllClick: (String) -> Unit
) {
    val upcomingEvents = eventsViewModel.upcomingEvents
    val nearbyEvents = eventsViewModel.nearbyEvents
    val recommendedEvents = eventsViewModel.recommendedEvents

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Upcoming Events Section ---
        if (upcomingEvents.isNotEmpty()) {
            EventSection(
                title = "Upcoming Events",
                events = upcomingEvents,
                onEventClick = onEventClick,
                onSeeAllClick = { onSeeAllClick("Upcoming Events") }
            )
        } else {
            EventSectionPlaceholder(title = "Upcoming Events")
        }

        // --- Nearby Events Section ---
        if (nearbyEvents.isNotEmpty()) {
            EventSection(
                title = "Nearby Events",
                events = nearbyEvents,
                onEventClick = onEventClick,
                onSeeAllClick = { onSeeAllClick("Nearby Events") }
            )
        } else {
            EventSectionPlaceholder(title = "Nearby Events")
        }

        // --- Recommended Events Section ---
        if (recommendedEvents.isNotEmpty()) {
            EventSection(
                title = "You may like",
                events = recommendedEvents,
                onEventClick = onEventClick,
                onSeeAllClick = { onSeeAllClick("Recommended Events") }
            )
        } else {
            EventSectionPlaceholder(title = "You may like")
        }
    }
}

@Composable
private fun EventSection(
    title: String,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onSeeAllClick: () -> Unit
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
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        // Horizontal List of Events
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Use a composite key from name and startDate.
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
            )

            // Text content section
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Event Details (Date, Location)
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
                            text = event.startDate,
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
                            text = event.location,
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(3) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(115.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.4f))
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.8f)
                        .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Column {
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.5f)
                            .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.7f)
                            .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
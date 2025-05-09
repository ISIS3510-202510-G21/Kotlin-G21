package com.isis3510.growhub.view.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.viewmodel.ConnectivityViewModel
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.viewmodel.HomeEventsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventsView(
    modifier: Modifier = Modifier,
    eventsViewModel: HomeEventsViewModel = viewModel(),
    connectivityViewModel: ConnectivityViewModel = viewModel(),
    onEventClick: (Event) -> Unit
) {
    val upcomingEvents by eventsViewModel.upcomingEvents
    val nearbyEvents by eventsViewModel.nearbyEvents
    val recommendedEvents by eventsViewModel.recommendedEvents
    val isLoadingUpcoming by eventsViewModel.isLoadingUpcoming
    val isLoadingNearby by eventsViewModel.isLoadingNearby
    val isLoadingRecommended by eventsViewModel.isLoadingRecommended

    val isLoadingMoreUpcoming by eventsViewModel.isLoadingMoreUpcoming
    val isLoadingMoreNearby by eventsViewModel.isLoadingMoreNearby
    val isLoadingMoreRecommended by eventsViewModel.isLoadingMoreRecommended

    val listStateUpcoming = rememberLazyListState()
    val listStateNearby = rememberLazyListState()
    val listStateRecommended = rememberLazyListState()

    val isNetworkAvailable by connectivityViewModel.networkStatus.collectAsState()

    // Effects for pagination
    LaunchedEffect(listStateUpcoming, upcomingEvents, isLoadingMoreUpcoming) {
        snapshotFlow { listStateUpcoming.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("EventsView", "Upcoming Events - Scrolled near end detected")
                if (!isLoadingMoreUpcoming && upcomingEvents.isNotEmpty() && !eventsViewModel.hasReachedEndUpcoming.value) {
                    Log.d("EventsView", "Upcoming Events - Loading more events")
                    eventsViewModel.loadMoreUpcomingEvents()
                } else {
                    Log.d("EventsView", "Upcoming Events - Not loading more events: isLoadingMoreUpcoming=$isLoadingMoreUpcoming, isEmpty=${upcomingEvents.isEmpty()}, hasReachedEndUpcoming=${eventsViewModel.hasReachedEndUpcoming.value}")
                }
            }
    }

    LaunchedEffect(listStateNearby, nearbyEvents, isLoadingMoreNearby) {
        snapshotFlow { listStateNearby.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("EventsView", "Nearby Events - Scrolled near end detected")
                if (!isLoadingMoreNearby && nearbyEvents.isNotEmpty() && !eventsViewModel.hasReachedEndNearby.value) {
                    Log.d("EventsView", "Nearby Events - Loading more events")
                    eventsViewModel.loadMoreNearbyEvents()
                } else {
                    Log.d("EventsView", "Nearby Events - Not loading more events: isLoadingMoreNearby=$isLoadingMoreNearby, isEmpty=${nearbyEvents.isEmpty()}, hasReachedEndNearby=${eventsViewModel.hasReachedEndNearby.value}")
                }
            }
    }

    LaunchedEffect(listStateRecommended, recommendedEvents, isLoadingMoreRecommended) {
        snapshotFlow { listStateRecommended.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("EventsView", "Recommended Events - Scrolled near end detected")
                if (!isLoadingMoreRecommended && recommendedEvents.isNotEmpty() && !eventsViewModel.hasReachedEndRecommended.value) {
                    Log.d("EventsView", "Recommended Events - Loading more events")
                    eventsViewModel.loadMoreRecommendedEvents()
                } else {
                    Log.d("EventsView", "Recommended Events - Not loading more events: isLoadingMoreRecommended=$isLoadingMoreRecommended, isEmpty=${recommendedEvents.isEmpty()}, hasReachedEndRecommended=${eventsViewModel.hasReachedEndRecommended.value}")
                }
            }
    }


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Upcoming Events Section ---
        if (isLoadingUpcoming) {
            EventSectionPlaceholder()
        } else if (upcomingEvents.isNotEmpty()) {
            EventSection(
                title = "Upcoming Events",
                events = upcomingEvents,
                onEventClick = onEventClick,
                listState = listStateUpcoming,
                isLoadingMore = isLoadingMoreUpcoming
            )
        } else if (isNetworkAvailable != ConnectionStatus.Available) {
            EventSectionEmpty(title = "Upcoming Events")
        }

        // --- Nearby Events Section ---
        if (isLoadingNearby) {
            EventSectionPlaceholder()
        } else if (nearbyEvents.isNotEmpty()){
            EventSection(
                title = "Nearby Events",
                events = nearbyEvents,
                onEventClick = onEventClick,
                listState = listStateNearby,
                isLoadingMore = isLoadingMoreNearby
            )
        } else if (isNetworkAvailable != ConnectionStatus.Available){
            EventSectionEmpty(title = "Nearby Events")
        }

        // --- Recommended Events Section ---
        if (isLoadingRecommended) {
            EventSectionPlaceholder()
        } else if (recommendedEvents.isNotEmpty()) {
            EventSection(
                title = "You may like",
                events = recommendedEvents,
                onEventClick = onEventClick,
                listState = listStateRecommended,
                isLoadingMore = isLoadingMoreRecommended
            )
        } else if (isNetworkAvailable != ConnectionStatus.Available) {
            EventSectionEmpty(title = "You may like")
        }
    }
}

@Composable
fun EventSectionEmpty(
    title: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
    onEventClick: (Event) -> Unit,
    listState: LazyListState,
    isLoadingMore: Boolean
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
            state = listState,
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
            if (isLoadingMore) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp).size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
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
                            text = event.location.getInfo(), // Assume location is usually present
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
private fun EventSectionPlaceholder() {
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
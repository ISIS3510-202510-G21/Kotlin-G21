package com.isis3510.growhub.view.home

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.offline.NetworkUtils
import com.isis3510.growhub.viewmodel.ConnectivityViewModel
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.view.navigation.Destinations
import com.isis3510.growhub.viewmodel.HomeEventsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventsView(
    modifier: Modifier = Modifier,
    eventsViewModel: HomeEventsViewModel = viewModel(),
    navController: NavHostController
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

    val hasReachedEndUpcoming by eventsViewModel.hasReachedEndUpcoming
    val hasReachedEndNearby by eventsViewModel.hasReachedEndNearby
    val hasReachedEndRecommended by eventsViewModel.hasReachedEndRecommended

    val connectivityViewModel: ConnectivityViewModel = viewModel()
    val isOffline by eventsViewModel.isOffline

    val listStateUpcoming = rememberLazyListState()
    val listStateNearby = rememberLazyListState()
    val listStateRecommended = rememberLazyListState()

    val networkStatus by connectivityViewModel.networkStatus.collectAsState()
    val context = LocalContext.current

    // Effects for pagination
    LaunchedEffect(listStateUpcoming, upcomingEvents, isLoadingMoreUpcoming) {
        snapshotFlow { listStateUpcoming.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("EventsView", "Upcoming Events - Scrolled near end detected")
                if (!isLoadingMoreUpcoming && upcomingEvents.isNotEmpty()) {
                    if (hasReachedEndUpcoming) {
                        if (isOffline) {
                            Toast.makeText(context, "Please check your connection", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No more events found", Toast.LENGTH_SHORT).show()
                        }
                    } else if (!NetworkUtils.isNetworkAvailable(context)) {
                        Toast.makeText(context, "Please check your connection", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("EventsView", "Upcoming Events - Loading more events")
                        eventsViewModel.loadMoreUpcomingEvents()
                    }
                } else {
                    Log.d("EventsView", "Upcoming Events - Not loading more events: isLoadingMoreUpcoming=$isLoadingMoreUpcoming, isEmpty=${upcomingEvents.isEmpty()}, hasReachedEndUpcoming=$hasReachedEndUpcoming")
                }
            }
    }

    LaunchedEffect(listStateNearby, nearbyEvents, isLoadingMoreNearby) {
        snapshotFlow { listStateNearby.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("EventsView", "Nearby Events - Scrolled near end detected")
                if (!isLoadingMoreNearby && nearbyEvents.isNotEmpty()) {
                    if (hasReachedEndNearby) {
                        if (isOffline) {
                            Toast.makeText(context, "Please check your connection", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No more events found", Toast.LENGTH_SHORT).show()
                        }
                    } else if (!NetworkUtils.isNetworkAvailable(context)) {
                        Toast.makeText(context, "Please check your connection", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("EventsView", "Nearby Events - Loading more events")
                        eventsViewModel.loadMoreNearbyEvents()
                    }
                } else {
                    Log.d("EventsView", "Nearby Events - Not loading more events: isLoadingMoreNearby=$isLoadingMoreNearby, isEmpty=${nearbyEvents.isEmpty()}, hasReachedEndNearby=$hasReachedEndNearby")
                }
            }
    }

    LaunchedEffect(listStateRecommended, recommendedEvents, isLoadingMoreRecommended) {
        snapshotFlow { listStateRecommended.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("EventsView", "Recommended Events - Scrolled near end detected")
                if (!isLoadingMoreRecommended && recommendedEvents.isNotEmpty()) {
                    if (hasReachedEndRecommended) {
                        if (isOffline) {
                            Toast.makeText(context, "Please check your connection", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No more events found", Toast.LENGTH_SHORT).show()
                        }
                    } else if (!NetworkUtils.isNetworkAvailable(context)) {
                        Toast.makeText(context, "Please check your connection", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("EventsView", "Recommended Events - Loading more events")
                        eventsViewModel.loadMoreRecommendedEvents()
                    }
                } else {
                    Log.d("EventsView", "Recommended Events - Not loading more events: isLoadingMoreRecommended=$isLoadingMoreRecommended, isEmpty=${recommendedEvents.isEmpty()}, hasReachedEndRecommended=$hasReachedEndRecommended")
                }
            }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Upcoming Events Section ---
        if (isLoadingUpcoming) {
            EventSectionPlaceholder(title = "Upcoming Events")
        } else if (upcomingEvents.isNotEmpty()) {
            EventSection(
                title = "Upcoming Events",
                events = upcomingEvents,
                navController = navController,
                listState = listStateUpcoming,
                isLoadingMore = isLoadingMoreUpcoming
            )
        } else {
            EventSectionEmpty(title = "Upcoming Events")
        }

        // --- Nearby Events Section ---
        if (isLoadingNearby) {
            EventSectionPlaceholder(title = "Nearby Events")
        } else if (nearbyEvents.isNotEmpty()){
            EventSection(
                title = "Nearby Events",
                events = nearbyEvents,
                navController = navController,
                listState = listStateNearby,
                isLoadingMore = isLoadingMoreNearby
            )
        } else {
            EventSectionEmpty(title = "Nearby Events")
        }

        // --- Recommended Events Section ---
        if (isLoadingRecommended) {
            EventSectionPlaceholder(title = "You may like")
        } else if (recommendedEvents.isNotEmpty()) {
            EventSection(
                title = "You may like",
                events = recommendedEvents,
                navController = navController,
                listState = listStateRecommended,
                isLoadingMore = isLoadingMoreRecommended
            )
        } else {
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
                    text = "No events available",
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
    navController: NavHostController,
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
                    navController = navController
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
private fun EventCard(event: Event, navController: NavHostController) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(240.dp)
            .height(215.dp)
            .clickable {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    val encoded = Uri.encode(event.name)
                    navController.navigate("${Destinations.EVENT_DETAIL}/$encoded")
                } else {
                    Toast
                        .makeText(context, "To see details, please check your internet connection", Toast.LENGTH_SHORT)
                        .show()
                }
            },
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
private fun EventSectionPlaceholder(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show actual title even in placeholder
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
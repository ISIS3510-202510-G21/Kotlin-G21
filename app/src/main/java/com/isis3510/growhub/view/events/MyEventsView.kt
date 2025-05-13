package com.isis3510.growhub.view.events

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.R
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.view.home.isScrolledNearEnd
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.viewmodel.ConnectivityViewModel
import com.isis3510.growhub.viewmodel.MyEventsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyEventsView(
    myEventsViewModel: MyEventsViewModel = viewModel(),
    connectivityViewModel: ConnectivityViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    val isNetworkAvailable by connectivityViewModel.networkStatus.collectAsState()

    val upcomingEvents by myEventsViewModel.upcomingEvents
    val previousEvents by myEventsViewModel.previousEvents
    val createdByMeEvents by myEventsViewModel.createdByMeEvents

    val isLoadingUpcoming by myEventsViewModel.isLoadingUpcoming
    val isLoadingMoreUpcoming by myEventsViewModel.isLoadingMoreUpcoming

    val isLoadingPrevious by myEventsViewModel.isLoadingPrevious
    val isLoadingMorePrevious by myEventsViewModel.isLoadingMorePrevious

    val isLoadingCreatedByMe by myEventsViewModel.isLoadingCreatedByMe
    val isLoadingMoreCreatedByMe by myEventsViewModel.isLoadingMoreCreatedByMe

    val listStateUpcoming = rememberLazyListState()
    val listStatePrevious = rememberLazyListState()
    val listStateCreatedByMe = rememberLazyListState()

    var showDialog by remember { mutableStateOf(false) }
    var eventIdToDelete by remember { mutableStateOf<String?>(null) }

    val currentStatus by connectivityViewModel.networkStatus.collectAsState()
    val initialNetworkAvailable = remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {

        if (initialNetworkAvailable.value == null) {
            initialNetworkAvailable.value = currentStatus == ConnectionStatus.Available
        }
    }

    LaunchedEffect(listStateUpcoming, upcomingEvents, isLoadingMoreUpcoming) {
        snapshotFlow { listStateUpcoming.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("MyEventsView", "My Events - Scrolled near end detected")
                if (!isLoadingMoreUpcoming && upcomingEvents.isNotEmpty() && !myEventsViewModel.hasReachedEnd.value) {
                    Log.d("MyEventsView", "My Events - Loading more events")
                    myEventsViewModel.loadMoreUpcomingEvents()
                } else {
                    Log.d(
                        "MyEventsView",
                        "Not loading more: isLoadingMore=$isLoadingMoreUpcoming, isEmpty=${upcomingEvents.isEmpty()}, hasReachedEnd=${myEventsViewModel.hasReachedEnd.value}"
                    )
                }
            }
    }

    LaunchedEffect(listStatePrevious, previousEvents, isLoadingMorePrevious) {
        snapshotFlow { listStatePrevious.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("MyEventsView", "My Events - Scrolled near end detected")
                if (!isLoadingMorePrevious && previousEvents.isNotEmpty() && !myEventsViewModel.hasReachedEnd.value) {
                    Log.d("MyEventsView", "My Events - Loading more events")
                    myEventsViewModel.loadMorePreviousEvents()
                } else {
                    Log.d(
                        "MyEventsView",
                        "Not loading more: isLoadingMore=$isLoadingMorePrevious, isEmpty=${previousEvents.isEmpty()}, hasReachedEnd=${myEventsViewModel.hasReachedEnd.value}"
                    )
                }
            }
    }

    LaunchedEffect(listStateCreatedByMe, createdByMeEvents, isLoadingMoreCreatedByMe) {
        snapshotFlow { listStateCreatedByMe.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("MyEventsView", "My Events - Scrolled near end detected")
                if (!isLoadingMoreCreatedByMe && createdByMeEvents.isNotEmpty() && !myEventsViewModel.hasReachedEnd.value) {
                    Log.d("MyEventsView", "My Events - Loading more events")
                    myEventsViewModel.loadMoreCreatedByMeEvents()
                } else {
                    Log.d(
                        "MyEventsView",
                        "Not loading more: isLoadingMore=$isLoadingMoreCreatedByMe, isEmpty=${createdByMeEvents.isEmpty()}, hasReachedEnd=${myEventsViewModel.hasReachedEnd.value}"
                    )
                }
            }
    }

    Scaffold(
        topBar = { MyEventsTopBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- UPCOMING EVENTS SECTION ---
            item {
                MyEventsSectionTitle(title = "Upcoming Events")
            }

            if (isLoadingUpcoming) {
                item {
                    MyEventsCardPlaceholder()
                }
            }

            else if (upcomingEvents.isNotEmpty()) {
                items(upcomingEvents) { event ->
                    MyEventsCard(event, onDelete = {
                        eventIdToDelete = event.name
                        showDialog = true
                    })
                }

                if (isLoadingMoreUpcoming) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
            else if (upcomingEvents.isEmpty()) {
                item {
                    MyEventsSectionEmpty()
                }
            }
            else if (isNetworkAvailable == ConnectionStatus.Unavailable) {
                item {
                    MyEventsSectionEmptyConnection()
                }
            } else if (initialNetworkAvailable.value == false) {
                item {
                    MyEventsSectionEmptyConnection()
                }
            }

            // --- PREVIOUS EVENTS SECTION ---
            item {
                MyEventsSectionTitle(title = "Previous Events")
            }

            if (isLoadingPrevious) {
                item {
                    MyEventsCardPlaceholder()
                }
            }
            else if (previousEvents.isNotEmpty()) {
                items(previousEvents) { event ->
                    MyEventsCard(event, onDelete = {
                        eventIdToDelete = event.name
                        showDialog = true
                    })
                }

                if (isLoadingMorePrevious) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
            else if (previousEvents.isEmpty()) {
                item {
                    MyEventsSectionEmpty()
                }
            }
            else if (isNetworkAvailable == ConnectionStatus.Unavailable){
                item {
                    MyEventsSectionEmptyConnection()
                }
            } else if (initialNetworkAvailable.value == false) {
                item {
                    MyEventsSectionEmptyConnection()
                }
            }

            // --- CREATED BY ME EVENTS SECTION ---
            item {
                MyEventsSectionTitle(title = "Created By Me Events")
            }

            if (isLoadingCreatedByMe) {
                item {
                    MyEventsCardPlaceholder()
                }
            } else if (createdByMeEvents.isNotEmpty()) {
                items(createdByMeEvents) { event ->
                    MyEventsCard(event, onDelete = {
                        eventIdToDelete = event.name
                        showDialog = true
                    })
                }

                if (isLoadingMoreCreatedByMe) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
            else if (createdByMeEvents.isEmpty()) {
                item {
                    MyEventsSectionEmpty()
                }
            }
            else if (isNetworkAvailable == ConnectionStatus.Unavailable){
                item {
                    MyEventsSectionEmptyConnection()
                }
            } else if (initialNetworkAvailable.value == false) {
                item {
                    MyEventsSectionEmptyConnection()
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        if (showDialog && eventIdToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    eventIdToDelete = null
                },
                title = { Text("Remove Event") },
                text = { Text("Are you sure you want to remove this event from your list?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            eventIdToDelete?.let { myEventsViewModel.removeUserFromEvent(it) }
                            showDialog = false
                            eventIdToDelete = null
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            eventIdToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 50.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar(navController = navController)
        }

    }
}

@Composable
fun MyEventsTopBar(onNavigateBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "My Events",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MyEventsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun MyEventsCard(event: Event, onDelete: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.Start) {
                    Image(
                        painter = rememberAsyncImagePainter(event.imageUrl),
                        contentDescription = event.name,
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (event.cost > 0.0) {
                        Icon(
                            painter = painterResource(id = R.drawable.payments),
                            contentDescription = "Paid Event",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF8BC34A)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = event.startDate, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = event.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                IconButton(onClick = { onDelete() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(
                        0xFFBE2927
                    )
                    )
                }
            }
        }
    }
}

@Composable
fun MyEventsCardPlaceholder() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.Start) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .fillMaxWidth(0.4f)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth(0.8f)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Top-right icon placeholder
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(4.dp))
                )
            }

            // Bottom-right icon placeholder
            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun MyEventsSectionEmpty() {
    Column(modifier = Modifier.fillMaxWidth()) {
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
fun MyEventsSectionEmptyConnection() {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    contentDescription = "No internet connection icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Internet Connection",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
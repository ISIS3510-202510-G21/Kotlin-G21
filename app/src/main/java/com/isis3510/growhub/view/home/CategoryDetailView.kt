package com.isis3510.growhub.view.events

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.view.home.isScrolledNearEnd
import com.isis3510.growhub.viewmodel.CategoryDetailViewModel
import com.isis3510.growhub.viewmodel.ConnectivityViewModel
import com.isis3510.growhub.viewmodel.EventsSorting
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@Composable
fun CategoryDetailView(
    categoryName: String,
    categoryEventsViewModel: CategoryDetailViewModel = viewModel(),
    connectivityViewModel: ConnectivityViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    firebaseAnalytics: FirebaseAnalytics
) {
    val isNetworkAvailable by connectivityViewModel.networkStatus.collectAsState()
    val initialNetworkAvailable = remember { mutableStateOf<Boolean?>(null) }

    // Initialize the ViewModel with the category name
    LaunchedEffect(categoryName) {
        categoryEventsViewModel.initialize(categoryName)
    }

    val filteredEvents by categoryEventsViewModel.filteredEvents
    val isLoading by categoryEventsViewModel.isLoading
    val isLoadingMore by categoryEventsViewModel.isLoadingMore

    val listState = rememberLazyListState()

    LaunchedEffect(listState, filteredEvents, isLoadingMore) {
        snapshotFlow { listState.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("CategoryEventView", "Category Events - Scrolled near end detected")
                if (!isLoadingMore && filteredEvents.isNotEmpty() && !categoryEventsViewModel.hasReachedEnd.value) {
                    Log.d("CategoryEventView", "Category Events - Loading more events")
                    categoryEventsViewModel.loadMoreCategoryEvents()
                } else {
                    Log.d(
                        "CategoryEventView",
                        "Not loading more: isLoadingMore=$isLoadingMore, isEmpty=${filteredEvents.isEmpty()}, hasReachedEnd=${categoryEventsViewModel.hasReachedEnd.value}"
                    )
                }
            }
    }

    Scaffold(
        topBar = { CategoryEventTopBar(categoryName, onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    EventCardPlaceholder()
                }
            } else if (filteredEvents.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryFilters(categoryEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(filteredEvents) { event ->
                        EventCard(event)
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            } else if (filteredEvents.isEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryFilters(categoryEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        EventSectionEmpty()
                    }
                }
            } else if (isNetworkAvailable == ConnectionStatus.Unavailable) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryFilters(categoryEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        EventSectionEmptyConnection()
                    }
                }
            } else if (initialNetworkAvailable.value == false) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryFilters(categoryEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        EventSectionEmptyConnection()
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryEventTopBar(categoryName: String, onNavigateBack: () -> Unit = {}) {
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
                text = categoryName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun CategoryFilters(viewModel: CategoryDetailViewModel, firebaseAnalytics: FirebaseAnalytics) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            val typesList = listOf("Free", "Paid")
            FilterDropdown(
                firebaseAnalytics,
                viewModel,
                "By Type",
                viewModel.selectedType.value,
                typesList
            ) {
                viewModel.selectedType.value = it
                viewModel.applyFilters()
            }

            Spacer(modifier = Modifier.width(8.dp))

            val sortingOptions = listOf(
                EventsSorting.SOONEST_TO_LATEST.name,
                EventsSorting.LATEST_TO_SOONEST.name
            )
            FilterDropdown(
                firebaseAnalytics,
                viewModel,
                "Sort By",
                viewModel.selectedSorting.value,
                sortingOptions
            ) {
                viewModel.selectedSorting.value = it
                viewModel.applyFilters()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.clearFilters()
                val bundle = Bundle().apply {
                    putString("filter_selected", "Clear")
                    putString("filter_label", "Clear Filters")
                }
                firebaseAnalytics.logEvent("category_events_filter", bundle)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear Icon",
                tint = Color.White
            )
            Text("Clear Filters", color = Color.White)
        }
    }
}

@Composable
fun FilterDropdown(
    firebaseAnalytics: FirebaseAnalytics,
    viewModel: CategoryDetailViewModel,
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF5669FF),
                contentColor = Color.White)) {
            Text(selectedOption.ifBlank { label })
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White)
        }

        val maxItems = 5
        val itemHeight = 48.dp
        val height: Dp = if (options.size > maxItems) {
            itemHeight * maxItems
        } else {
            itemHeight * options.size
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.height(height)) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                        val bundle = Bundle().apply {
                            putString("filter_selected", option)
                            putString("filter_label", label)
                        }
                        firebaseAnalytics.logEvent("category_events_filter", bundle)
                    },
                    text = { Text(option.ifBlank { "All" }) }
                )
                if (option != options.last()) {
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}

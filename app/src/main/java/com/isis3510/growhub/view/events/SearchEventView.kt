package com.isis3510.growhub.view.events

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.view.home.isScrolledNearEnd
import com.isis3510.growhub.viewmodel.ConnectivityViewModel
import com.isis3510.growhub.viewmodel.SearchEventViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchEventView(
    searchEventsViewModel: SearchEventViewModel = viewModel(),
    connectivityViewModel: ConnectivityViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    firebaseAnalytics: FirebaseAnalytics
) {
    val isNetworkAvailable by connectivityViewModel.networkStatus.collectAsState()
    val initialNetworkAvailable = remember { mutableStateOf<Boolean?>(null) }

    val searchEvents by searchEventsViewModel.searchEvents
    val isLoading by searchEventsViewModel.isLoading
    val isLoadingMore by searchEventsViewModel.isLoadingMore

    val listStateSearch = rememberLazyListState()

    LaunchedEffect(listStateSearch, searchEvents, isLoadingMore) {
        snapshotFlow { listStateSearch.isScrolledNearEnd() }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                Log.d("SearchEventView", "Search Events - Scrolled near end detected")
                if (!isLoadingMore && searchEvents.isNotEmpty() && !searchEventsViewModel.hasReachedEnd.value) {
                    Log.d("SearchEventView", "Search Events - Loading more events")
                    searchEventsViewModel.loadMoreSearchEvents()
                } else {
                    Log.d(
                        "SearchEventView",
                        "Not loading more: isLoadingMore=$isLoadingMore, isEmpty=${searchEvents.isEmpty()}, hasReachedEnd=${searchEventsViewModel.hasReachedEnd.value}"
                    )
                }
            }
    }

    Scaffold(
        topBar = { SearchEventTopBar(onNavigateBack) },
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
            } else if (searchEvents.isNotEmpty()) {
                LazyColumn(
                    state = listStateSearch,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        SearchBar(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchFilters(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(searchEventsViewModel.filteredEvents) { event ->
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
            }
            else if (searchEvents.isEmpty()) {
                LazyColumn(
                    state = listStateSearch,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        SearchBar(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchFilters(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        EventSectionEmpty()
                    }
                }
            }

            else if (isNetworkAvailable == ConnectionStatus.Unavailable) {
                LazyColumn(
                    state = listStateSearch,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        SearchBar(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchFilters(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        EventSectionEmptyConnection()
                    }
                }
            }
            else if (initialNetworkAvailable.value == false) {
                LazyColumn(
                    state = listStateSearch,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        SearchBar(searchEventsViewModel, firebaseAnalytics)
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchFilters(searchEventsViewModel, firebaseAnalytics)
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
fun SearchEventTopBar(onNavigateBack: () -> Unit = {}) {
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
                text = "Search",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchBar(viewModel: SearchEventViewModel, firebaseAnalytics: FirebaseAnalytics) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = viewModel.searchQuery,
        onValueChange = { if (it.length <= 50) {
            viewModel.searchQuery = it }
        },
        label = { Text("Search...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color(0xFF5669FF)
            )
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                val bundle = Bundle().apply {
                    putString("search_query", viewModel.searchQuery)
                }
                firebaseAnalytics.logEvent("search_events_interaction", bundle)
            }
        )
    )
}

@SuppressLint("RestrictedApi")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchFilters(viewModel: SearchEventViewModel, firebaseAnalytics: FirebaseAnalytics) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            val typesList = listOf("Free", "Paid")
            FilterDropdown(firebaseAnalytics, "By Type", viewModel.selectedType, typesList) {
                viewModel.selectedType = it
            }

            Spacer(modifier = Modifier.width(8.dp))

            val categoriesList = viewModel.categories.map { it.name }
            FilterDropdown(firebaseAnalytics,"By Category", viewModel.selectedCategory, categoriesList) {
                viewModel.selectedCategory = it
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilterDropdown(firebaseAnalytics,"By Skill", viewModel.selectedSkill, viewModel.skills) {
                viewModel.selectedSkill = it
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilterDropdown(firebaseAnalytics,"By Location", viewModel.selectedLocation, viewModel.locations) {
                viewModel.selectedLocation = it
            }

            Spacer(modifier = Modifier.width(8.dp))

            DatePickerButton(
                selectedDate = viewModel.selectedDate,
                onDateSelected = { viewModel.selectedDate = it },
                firebaseAnalytics
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.clearFilters()
                val bundle = Bundle().apply {
                    putString("filter_selected", "Clear")
                    putString("filter_label", "Clear Filters")
                }
                firebaseAnalytics.logEvent("search_events_filter", bundle)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FilterDropdown(
    firebaseAnalytics: FirebaseAnalytics,
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
                contentColor = Color.White)){
            Text(selectedOption.ifBlank { label })
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White)
        }

        val maxItems = 5
        val itemHeight = 48.dp
        var height = Dp.Unspecified
        if (options.size > maxItems) {
            height = itemHeight * maxItems
        }
        else {
            height = itemHeight * options.size
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
                        firebaseAnalytics.logEvent("search_events_filter", bundle)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerButton(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    firebaseAnalytics: FirebaseAnalytics
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formatted = pickedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                onDateSelected(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    OutlinedButton(
        onClick = { datePickerDialog.show()
                  val bundle = Bundle().apply {
                      putString("filter_selected", selectedDate)
                      putString("filter_label", "By Date")
                  }
                  firebaseAnalytics.logEvent("search_events_filter", bundle)
                  },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5669FF),
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Pick date",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedDate.ifBlank { "By Date" }
            )
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.startDate, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = event.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = event.location.getInfo(), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EventCardPlaceholder() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.4f)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth(0.7f)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.3f)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun EventSectionEmpty() {
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
fun EventSectionEmptyConnection() {
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

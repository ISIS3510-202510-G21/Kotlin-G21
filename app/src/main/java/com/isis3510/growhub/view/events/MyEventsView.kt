package com.isis3510.growhub.view.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.R
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.viewmodel.Event
import com.isis3510.growhub.viewmodel.MyEventsViewModel

/**
 * Created by: Juan Manuel Jáuregui
 */

// Main View Function
@Composable
fun MainView() {
    MyEventsView()
}

// My Events View Function
@Composable
fun MyEventsView(viewModel : MyEventsViewModel = viewModel()) {

    val upcomingEvents = viewModel.upcomingEvents
    val previousEvents = viewModel.previousEvents

    //val upcomingEvents by remember { mutableStateOf(viewModel.upcomingEvents) }
    //val previousEvents by remember { mutableStateOf(viewModel.previousEvents) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "My Events",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { SectionTitle("Upcoming Events") }
            items(upcomingEvents, key = { it.id }) { event ->
                EventCard(event)
            }

            item { SectionTitle("Previous Events") }
            items(previousEvents, key = { it.id }) { event ->
                EventCard(event)
            }
        }
    }
}

// Section Title
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

// Event Card
@Composable
fun EventCard(event: Event) {

    // Card for each event
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        // Box for the information
        Box(modifier = Modifier.padding(8.dp)) {

            // Row for the Image + Text
            Row(modifier = Modifier.fillMaxWidth()) {

                // Column for Image + Payment Icon
                Column(horizontalAlignment = Alignment.Start) {

                    // Event Image
                    Image(
                        painter = painterResource(id = event.imageRes),
                        contentDescription = event.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    // Space between Image and Payment Icon
                    Spacer(modifier = Modifier.height(8.dp))

                    if (event.isPaid) { // Show payment icon if event is paid

                        // Payment Icon
                        Icon(
                            painter = painterResource(id = R.drawable.payments),
                            contentDescription = "Paid Event",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Space between Image and Text
                Spacer(modifier = Modifier.width(12.dp))

                // Column for text details
                Column(modifier = Modifier.weight(1f)) {

                    // Date and Title
                    Text(text = event.date, fontSize = 12.sp, color = Color(0xFF5669FF))
                    Text(
                        text = event.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Flag Icon (Top-Right Corner)
            Row (modifier = Modifier.align(Alignment.TopEnd)){
                IconButton(onClick = { /* Handle flag */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.flag),
                        contentDescription = "Flag"
                    )
                }
            }

            // Trash Icon (Bottom-Right Corner)
            Row (modifier = Modifier.align(Alignment.BottomEnd)){
                IconButton(onClick = { /* Handle delete */  }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

// Bottom Navigation Bar
@Composable
fun BottomNavigationBar() {
    val navItems = listOf("Home", "Map", "My Events", "Profile")
    var selectedItem by remember { mutableStateOf("My Events") }

    // Bottom Navigation Bar
    NavigationBar (containerColor = Color.White){
        navItems.forEachIndexed { index, item ->

            // Always keep Add Button in the center
            if (index == 2) {

                // Add Button
                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5669FF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    label = { Text("") },
                    selected = false,
                    onClick = {  /*Handle Add action*/  }
                )
            }

            // Normal navigation items
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = when (item) {
                                "Home" -> R.drawable.home
                                "Map" -> R.drawable.map
                                "My Events" -> R.drawable.events
                                "Profile" -> R.drawable.profile
                                else -> R.drawable.home
                            }
                        ),
                        contentDescription = item,
                        tint = if (selectedItem == item) Color(0xFF5669FF) else Color(0xFFDCD9D9),
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = item,
                        color = if (selectedItem == item) Color(0xFF5669FF) else Color(0xFFDCD9D9)
                    )
                },
                selected = selectedItem == item,
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White),
                onClick = { selectedItem = item }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyEventsPreview() {
    GrowhubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainView()
        }
    }
}
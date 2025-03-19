package com.isis3510.growhub.view.events

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.R
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.viewmodel.MyEventsViewModel

/**
 * Created by: Juan Manuel Jáuregui
 */

@Composable
fun MyEventsView(
    viewModel: MyEventsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    Scaffold(
        topBar = { MyEventsTopBar(onNavigateBack) },
        bottomBar = { BottomNavigationBar(navController = navController) },
        containerColor = Color.White
    ) { paddingValues ->
        MyEventsContent(viewModel, paddingValues)
    }
}

@Composable
fun MyEventsTopBar(onNavigateBack: () -> Unit = {}) {
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
}

@Composable
fun MyEventsContent(viewModel: MyEventsViewModel, paddingValues: PaddingValues) {
    val upcomingEvents = viewModel.upcomingEvents
    val previousEvents = viewModel.previousEvents

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item { MyEventsSectionTitle("Upcoming Events") }
        items(upcomingEvents, key = { it.id }) { event ->
            MyEventsCard(event)
        }

        item { MyEventsSectionTitle("Previous Events") }
        items(previousEvents, key = { it.id }) { event ->
            MyEventsCard(event)
        }
    }
}

@Composable
fun MyEventsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun MyEventsCard(event: Event) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.Start) {
                    Image(
                        painter = rememberAsyncImagePainter(event.imageUrl),
                        contentDescription = event.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (event.cost > 0.0) {
                        Icon(
                            painter = painterResource(id = R.drawable.payments),
                            contentDescription = "Paid Event",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = event.date, fontSize = 18.sp, color = Color(0xFF5669FF))
                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { /* Handle flag */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.flag),
                        contentDescription = "Flag"
                    )
                }
            }
            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                IconButton(onClick = { /* Handle delete */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyEventsPreview() {

    val navController = rememberNavController()

    GrowhubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyEventsView(navController = navController, onNavigateBack = {})
        }
    }
}
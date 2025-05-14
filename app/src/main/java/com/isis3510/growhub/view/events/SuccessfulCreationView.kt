package com.isis3510.growhub.view.events

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.viewmodel.SuccessfulCreationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SuccessfulCreationView(
    eventName: String,
    onMyEvents: () -> Unit = {},
    viewModel: SuccessfulCreationViewModel = viewModel()
) {

    LaunchedEffect(eventName) {
        viewModel.loadEvent(eventName)
    }

    val event by viewModel.event

    Scaffold(
        topBar = { CreationTopBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        val scrollState = rememberScrollState()

        if (event != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CreationSuccessBanner()
                EventCreationDetailsTitle()

                EventCard(
                    name = event!!.name,
                    creator = event!!.creator,
                    cost = event!!.cost,
                    attendees = event!!.attendees
                )

                val address = event!!.location.address
                val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                val date = inputFormat.parse(event!!.startDate)
                val formattedDate = date?.let { SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(it) }
                val formattedTime = date?.let { SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(it) }

                InfoSection(
                    startDate = formattedDate.toString(),
                    startTime = formattedTime.toString(),
                    category = event!!.category,
                    skills = event!!.skills,
                    location = address
                )

                Spacer(modifier = Modifier.height(16.dp))
                BackToHomeButton(onBackToHome = onMyEvents)

                Spacer(modifier = Modifier.height(64.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun CreationTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Event Creation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun CreationSuccessBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2ECC71)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Event Created Successfully",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
fun EventCreationDetailsTitle() {
    Text(
        text = "Event Creation Details",
        modifier = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}

@Composable
fun BackToHomeButton(onBackToHome: () -> Unit = {}) {
    Button(
        onClick = { onBackToHome() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5669FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Back to Home", color = Color.White)
    }
}

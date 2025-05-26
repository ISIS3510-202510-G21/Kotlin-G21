package com.isis3510.growhub.view.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.viewmodel.SuccessfulCreationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SuccessfulCreationView(
    eventName: String,
    isQueued: Boolean,
    onMyEvents: () -> Unit = {},
    viewModel: SuccessfulCreationViewModel = viewModel()
) {

    LaunchedEffect(eventName, isQueued) {
        viewModel.loadEvent(eventName, isQueued)
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CreationSuccessBanner(queued = isQueued)

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

                BackToHomeButton(onBackToHome = onMyEvents)

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
fun CreationSuccessBanner(queued: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = if (queued) { CardDefaults.cardColors(containerColor = Color(0xFFFFC107))} else {
            CardDefaults.cardColors(containerColor = Color(0xFF2ECC71))
        },
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
                    imageVector = if (queued) Icons.Default.MailOutline else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (!queued) {"Event Created Successfully"} else {
                        "You are Offline: Event scheduled for creation"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
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
        Text("My Events", color = Color.White)
    }
}

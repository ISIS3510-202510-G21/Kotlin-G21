package com.isis3510.growhub.view.events

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.isis3510.growhub.viewmodel.SuccessfulRegistrationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SuccessfulRegistrationView(
    eventName: String,
    onMyEvents: () -> Unit = {},
    viewModel: SuccessfulRegistrationViewModel = viewModel()
) {
    LaunchedEffect(eventName) {
        viewModel.loadEvent(eventName)
    }

    val event by viewModel.event

    Scaffold(
        topBar = {
            EventTopBar()
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = { innerPadding ->

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
                    SuccessBanner()
                    EventRegistrationDetailsTitle()

                    EventCard(
                        name = event!!.name,
                        creator = event!!.creator,
                        cost = event!!.cost,
                        attendees = event!!.attendees
                    )

                    val address = event!!.location.address
                    val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                    val startDate = event!!.startDate
                    val date = inputFormat.parse(startDate)

                    val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH)
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

                    val formattedDate = date?.let { dateFormat.format(it) }
                    val formattedTime = date?.let { timeFormat.format(it) }

                    InfoSection(
                        startDate = formattedDate.toString(),
                        startTime = formattedTime.toString(),
                        category = event!!.category,
                        skills = event!!.skills,
                        location = address
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    MyEventsButton(onMyEvents = onMyEvents)

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
    )
}

@Composable
fun EventTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Event Registration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SuccessBanner() {
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
                    text = "Event Booked Successfully",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
fun EventRegistrationDetailsTitle() {
    Text(
        text = "Event Registration Details",
        modifier = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}

@Composable
fun EventCard(name: String, creator: String, cost: Int, attendees: List<String>) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(color = Color(0xFF5669FF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start, // Important!
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "By $creator",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cost
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cost",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(30.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Cost",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (cost == 0) "Free" else "$$cost",
                                fontSize = 14.sp,
                                color = Color(0xFF1E88E5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                    )

                    Spacer(modifier = Modifier.width(48.dp))

                    // Attendees
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Attendees",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(30.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Attendees",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = attendees.size.toString() +" people",
                                fontSize = 14.sp,
                                color = Color(0xFF1E88E5)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun InfoSection(startDate: String, startTime: String, category: String, skills: List<String>, location: String) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(label = "Time", value = startTime)
            InfoRow(label = "Date", value = startDate)
            InfoRow(label = "Category", value = category)
            InfoRow(label = "Skills", value = skills.joinToString(", "))
            InfoRow(
                label = "Location",
                value = location
            )
        }
    }
}

@Composable
fun MyEventsButton(onMyEvents: () -> Unit = {}) {
    Button(
        onClick = { onMyEvents() },
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

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

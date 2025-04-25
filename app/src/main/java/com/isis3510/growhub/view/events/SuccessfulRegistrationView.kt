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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SuccessfulRegistrationView(
    viewModel: SuccessfulRegistrationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            EventTopBar(onNavigateBack)
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                EventRegistrationContent(viewModel)
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventRegistrationContent(
    viewModel: SuccessfulRegistrationViewModel
) {
    val event = viewModel.registeredEvent
    if (event.isNotEmpty()) {
        val eventR = event[0]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SuccessBanner()
            EventRegistrationDetailsTitle()
            EventCard(eventR.name, eventR.creator, eventR.cost, eventR.attendees)
            InfoSection(eventR.startDate, eventR.category, eventR.skills, eventR.location.getInfo())
            Spacer(modifier = Modifier.height(16.dp))
            MyEventsButton()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EventTopBar(onNavigateBack: () -> Unit = {}) {
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
                text = "Event",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xff191d17),
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
                .padding(16.dp),
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
                colorFilter = ColorFilter.tint(color = Color(0xFF72796F))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(color = 0xFF72796F)
                )
                Text(
                    text = creator,
                    fontSize = 14.sp,
                    color = Color(color = 0xFF72796F)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cost",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Cost",
                                color = Color(color = 0xFF72796F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cost.toString(),
                                color = Color(0xFF1E88E5),
                                fontSize = 14.sp
                            )
                        }
                    }

                    VerticalDivider(
                        color = Color(0xFF72796F),
                        modifier = Modifier.height(16.dp).width(1.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Attendees",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Attendees",
                                color = Color(color = 0xFF72796F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = attendees.size.toString(),
                                color = Color(0xFF1E88E5),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(startDate: String, category: String, skills: List<String>, location: String) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Extract the start time from the start date
            val startTime = startDate.split(" ")[1]
            val date = startDate.split(" ")[0]
            InfoRow(label = "Time", value = startTime)
            InfoRow(label = "Date", value = date)
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
fun MyEventsButton() {
    Button(
        onClick = { /* TODO: Navigate to My Events */ },
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
        Text(text = value, color = Color.Gray)
    }
}

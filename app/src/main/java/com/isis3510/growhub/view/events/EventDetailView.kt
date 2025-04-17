package com.isis3510.growhub.view.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetailView(
    eventName: String,
    navController: NavHostController
) {
    /* ---------- Estado ---------- */
    var event by remember { mutableStateOf<Event?>(null) }
    var loading by remember { mutableStateOf(true) }


    /* ---------- Carga desde Firestore ---------- */
    LaunchedEffect(eventName) {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("events")
            .whereEqualTo("name", eventName)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val d = snapshot.documents[0]

            val categoryRef = d.getDocumentReference("category")
            val categoryName = try {
                categoryRef?.get()?.await()?.getString("name") ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }

            val locationRef = d.getDocumentReference("locations")
            val address = try {
                locationRef?.get()?.await()?.getString("address") ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }

            val city = try {
                locationRef?.get()?.await()?.getString("city") ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }

            val university = try {
                locationRef?.get()?.await()?.getBoolean("university") ?: false
            } catch (e: Exception) {
                false
            }

            val attendeesList = try {
                (d.get("attendees") as? List<*>)?.mapNotNull {
                    (it as? com.google.firebase.firestore.DocumentReference)?.get()?.await()?.getString("name")
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val skillsList = try {
                (d.get("skills") as? List<*>)?.mapNotNull { ref ->
                    (ref as? com.google.firebase.firestore.DocumentReference)?.get()?.await()?.getString("name")
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }



            event = Event(
                name         = d.getString("name") ?: "",
                description  = d.getString("description") ?: "",
                location     = address,
                city         = city,
                startDate    = d.getTimestamp("start_date")?.toDate()
                    ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                endDate      = d.getTimestamp("end_date")?.toDate()
                    ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                category = categoryName,
                imageUrl     = d.getString("image") ?: "",
                cost         = d.getDouble("cost")?.toInt() ?: 0,
                attendees = attendeesList,
                isUniversity = university,
                skills = skillsList,
            )
        }
        loading = false
    }

    /* ---------- UI ---------- */
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            event?.let { ev ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {
                        Image(
                            painter = rememberAsyncImagePainter(ev.imageUrl),
                            contentDescription = ev.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    item {
                        Text(
                            text = ev.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    /* --- Cost / Category / Location cards --- */
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoChip(label = "Cost", value = if (ev.cost == 0) "FREE" else "\$${ev.cost}")
                            InfoChip(label = "Category", value = ev.category)
                            InfoChip(label = "Location", value = ev.location)
                        }
                    }

                    /* --- Start & End --- */
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoChip(label = "Start", value = ev.startDate)
                            InfoChip(label = "End", value = ev.endDate)
                        }
                    }

                    /* --- Description --- */
                    item {
                        SectionCard(title = "Description") {
                            Text(ev.description, fontSize = 14.sp)
                        }
                    }

                    /* --- Skills --- */
                    if (ev.skills.isNotEmpty()) {
                        item {
                            SectionCard(title = "Skills") {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ev.skills.forEach { s ->
                                        AssistChip(onClick = {}, label = { Text(s) })
                                    }
                                }
                            }
                        }
                    }

                    /* --- Speaker (placeholder con creatorId) --- */
                    item {
                        SectionCard(title = "Speaker") {
                            Text(text = "Creator ID: ${ev.attendees.firstOrNull() ?: "N/A"}")
                        }
                    }

                    /* --- Book Event button (todavía inhabilitado) --- */
                    item {
                        Button(
                            onClick = { /* sin implementar */ },
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("Book Event")
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Helpers ---------- */

@Composable
private fun InfoChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFFF4F4F4), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 10.sp, color = Color(0xFF9A9A9A))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFF4F4F4), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

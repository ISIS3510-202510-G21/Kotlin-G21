package com.isis3510.growhub.view.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.R
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.model.objects.Location
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/* ---------- Paleta / estilos ---------- */
private val CardShape = RoundedCornerShape(12.dp)
private val CardBg     = Color.White
private val Accent = Color(0xFF5669FF)
private val ChipBg     = Color(0xFFF4F4F4)
private val ChipLabel  = Color(0xFF9A9A9A)
private val BodyText   = Color(0xFF191D17)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetailView(
    eventName: String,
    navController: NavHostController
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var loading by remember { mutableStateOf(true) }

    val inPreview = LocalInspectionMode.current
    if (inPreview && event == null) {
        event = Event(
            name = "IA Prompt Engineering",
            description = "En un mundo donde la inteligencia artificial ...",
            location = Location("Cra. 1 #18a‑12", "Bogotá", "Edificio ML",4.65, -74.05, false),
            startDate = "24 Nov 2025",
            endDate = "24 Nov 2025",
            category = "IA Engineers",
            imageUrl = "https://placehold.co/600x400/png",
            cost = 0,
            attendees = listOf("Miguel Durán"),
            skills = listOf("Programming"),
            creator = ""
        )
        loading = false
    }

    LaunchedEffect(eventName) {
        if (inPreview) return@LaunchedEffect

        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("events")
            .whereEqualTo("name", eventName)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        snapshot?.let { d ->
            suspend fun DocumentReference?.string(field: String): String =
                this?.get()?.await()?.getString(field) ?: "Unknown"

            val categoryName = d.getDocumentReference("category").string("name")
            val locationRef = d.getDocumentReference("location_id")
            val locationSnapshot = locationRef?.get()?.await()

            val location = if (locationSnapshot != null && locationSnapshot.exists()) {
                Location(
                    address = locationSnapshot.getString("address") ?: "Unknown",
                    city = locationSnapshot.getString("city") ?: "Unknown",
                    latitude = locationSnapshot.getDouble("latitude") ?: 0.0,
                    longitude = locationSnapshot.getDouble("longitude") ?: 0.0,
                    university = locationSnapshot.getBoolean("university") ?: false
                )
            } else {
                Location("Unknown", "Unknown", "Unknown",0.0, 0.0, false)
            }

            val attendees = (d["attendees"] as? List<*>)?.mapNotNull {
                (it as? DocumentReference)?.string("name")
            } ?: emptyList()

            val skills = (d["skills"] as? List<*>)?.mapNotNull {
                (it as? DocumentReference)?.string("name")
            } ?: emptyList()

            val creator = ""

            event = Event(
                name = d.getString("name") ?: "",
                description = d.getString("description") ?: "",
                location = location,
                startDate = d.getTimestamp("start_date")?.toDate()
                    ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                endDate = d.getTimestamp("end_date")?.toDate()
                    ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "",
                category = categoryName,
                imageUrl = d.getString("image") ?: "",
                cost = d.getDouble("cost")?.toInt() ?: 0,
                attendees = attendees,
                skills = skills,
                creator = creator
            )
        }
        loading = false
    }

    /* ------------------------- UI --------------------------------------- */
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "",
                            tint = Color.Black,
                            modifier = Modifier.size(29.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CardBg, titleContentColor = BodyText
                )
            )
        },
        containerColor = Color.White
    ) { padd ->
        if (loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else event?.let { ev ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padd),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                /* ---------- Banner ---------- */
                item {
                    Image(
                        painter = rememberAsyncImagePainter(ev.imageUrl),
                        contentDescription = ev.name,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(CardShape)
                            .background(Color.LightGray, shape = CardShape)
                    )
                }


                /* ---------- Nombre ---------- */
                item {
                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(ChipBg, CardShape) // ← fondo gris aplicado a todo el bloque
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            // Título dentro del gris
                            Text(
                                text = ev.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BodyText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Fila de chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InfoChip(
                                    label = "Cost",
                                    value = if (ev.cost == 0) "FREE" else "\$${ev.cost}",
                                    icon = painterResource(id = R.drawable.ic_money),
                                    modifier = Modifier.weight(1f)
                                )

                                VerticalDivider()

                                InfoChip(
                                    label = "Category",
                                    value = ev.category,
                                    icon = painterResource(id = R.drawable.ic_category),
                                    modifier = Modifier.weight(1f)
                                )

                                VerticalDivider()

                                InfoChip(
                                    label = "Location",
                                    value = ev.location.getInfo().ifBlank { "Unknown" },
                                    icon = painterResource(id = R.drawable.ic_pin),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }




                /* ---------- Start / End ---------- */
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip(
                            "Start",
                            ev.startDate,
                            painterResource(id = R.drawable.ic_calendar),
                            Modifier.weight(1f)
                        )
                        InfoChip(
                            "End",
                            ev.endDate,
                            painterResource(id = R.drawable.ic_calendar),
                            Modifier.weight(1f)
                        )
                    }
                }

                /* ---------- Description ---------- */
                /* ---------- Description ---------- */
                item {
                    SectionCard("Description") {
                        Column {
                            Text(ev.description, fontSize = 14.sp, color = BodyText, lineHeight = 18.sp)

                            if (ev.skills.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Skills", fontWeight = FontWeight.Bold, color = BodyText)
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ev.skills.forEach { s ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(s) },
                                            colors = AssistChipDefaults.assistChipColors(

                                                labelColor = BodyText
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }



                /* ---------- Speaker ---------- */
                if (ev.attendees.isNotEmpty()) {
                    item {
                        SectionCard("Speaker") {
                            Text(ev.attendees.first(), color = BodyText)
                        }
                    }
                }

                /* ---------- Botón ---------- */
                item {
                    Button(
                        onClick = {},
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("Book Event", color = Color.White)
                    }
                }
            }
        }
    }
}

/* ---------- COMPONENTES REUTILIZABLES ----------------------------------- */
@Composable
private fun InfoChip(
    label: String,
    value: String,
    icon: Painter,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(ChipBg, CardShape)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .width(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna izquierda: Ícono
        Column(
            modifier = Modifier.padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = ChipLabel,
                modifier = Modifier.size(20.dp)
            )
        }

        // Columna derecha: Texto
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = ChipLabel,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = Accent,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}


@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChipBg, CardShape)
                .padding(16.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = BodyText)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}


/* ---------- PREVIEW ----------------------------------------------------- */
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun EventDetailPreview() {
    val nav = rememberNavController()
    EventDetailView(eventName = "Preview", navController = nav)
}
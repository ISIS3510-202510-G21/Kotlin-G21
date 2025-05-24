package com.isis3510.growhub.view.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.R
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.viewmodel.EventDetailViewModel

private val CardShape = RoundedCornerShape(12.dp)
private val CardBg = Color.White
private val Accent = Color(0xFF5669FF)
private val ChipBg = Color(0xFFF4F4F4)
private val ChipLabel = Color(0xFF9A9A9A)
private val BodyText = Color(0xFF191D17)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetailView(
    eventName: String,
    navController: NavHostController,
    onBookEvent: () -> Unit = {},
    onAttendeesClick: () -> Unit = {},
    vm: EventDetailViewModel = viewModel()
) {
    val inPreview = LocalInspectionMode.current
    LaunchedEffect(eventName, inPreview) {
        vm.loadEvent(eventName, inPreview)
    }

    val event by vm.event
    val loading by vm.loading

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
            val eventImagePainter = rememberAsyncImagePainter(ev.imageUrl)

            val rememberedSkills = remember(ev.skills) { ev.skills }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padd),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Image(
                        painter = eventImagePainter,
                        contentDescription = ev.name,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(CardShape),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }

                item {
                    EventHeaderCard(ev, onAttendeesClick)
                }

                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip("Start", ev.startDate, painterResource(id = R.drawable.ic_calendar), Modifier.weight(1f))
                        InfoChip("End", ev.endDate, painterResource(id = R.drawable.ic_calendar), Modifier.weight(1f))
                    }
                }
                item {
                    SectionCard("Description") {
                        Column {
                            Text(ev.description, fontSize = 14.sp, color = BodyText, lineHeight = 18.sp)
                            if (rememberedSkills.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                Text("Skills", fontWeight = FontWeight.Bold, color = BodyText)
                                Spacer(Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rememberedSkills.forEach { s ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(s) },
                                            colors = AssistChipDefaults.assistChipColors(labelColor = BodyText)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (ev.attendees.isNotEmpty()) {
                    item {
                        SectionCard("Speaker") {
                            Text(ev.attendees.first(), color = BodyText)
                        }
                    }
                }
                item {
                    Button(
                        onClick = { onBookEvent() },
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

@Composable
private fun EventHeaderCard(ev: Event, onAttendeesClick: () -> Unit = {}) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = ChipBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(ev.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BodyText)
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip("Cost", if (ev.cost == 0) "FREE" else "\$${ev.cost}", painterResource(id = R.drawable.ic_money), Modifier.weight(1f))
                    VerticalDivider()
                    InfoChip("Category", ev.category, painterResource(id = R.drawable.ic_category), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip("Location", ev.location.getInfo().ifBlank { "Unknown" }, painterResource(id = R.drawable.ic_pin), Modifier.weight(1f))
                    VerticalDivider()
                    InfoChip(
                        label = "Attendees",
                        value = ev.attendees.size.toString() + if (ev.attendees.size == 1) " person" else " people",
                        icon = painterResource(id = R.drawable.ic_email),
                        modifier = Modifier.weight(1f),
                        onClick = { onAttendeesClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, icon: Painter, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(
        modifier = modifier
            .background(ChipBg, CardShape)
            .padding(vertical = 10.dp, horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = ChipLabel,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(20.dp)
        )
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
                maxLines = 1,
                modifier = if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun EventDetailPreview() {
    val nav = rememberNavController()
    EventDetailView(eventName = "Preview", navController = nav)
}

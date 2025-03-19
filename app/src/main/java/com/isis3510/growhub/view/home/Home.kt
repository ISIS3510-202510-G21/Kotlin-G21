package com.isis3510.growhub.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.R
import com.isis3510.growhub.viewmodel.HomeViewModel
import com.isis3510.growhub.view.theme.GrowhubTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.isis3510.growhub.utils.advancedShadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.isis3510.growhub.viewmodel.Event
import com.isis3510.growhub.view.BottomNavBar
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.viewmodel.Category
import com.isis3510.growhub.view.navigation.BottomNavigationBar

@Composable
fun MainView(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopBoxRenderer(onLogout = onLogout)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clipToBounds()
            ) {
                BottomNavigationBar()
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // This is the whole screen renderer
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .background(Color(0xffffffff))
                    .size(412.dp, 917.dp)
                    .clipToBounds(),
            ) {
                CategoryColorButtons()
                EventSliders()
            }
        }
    }
}

@Composable
fun TopBoxRenderer(
    // viewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit
) {
    // val uiState = viewModel.uiState.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Color(0xff4a43ec)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de ubicación y logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f)) // Empuja el texto al centro

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Location",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bogotá, Colombia",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // Empuja el icono a la derecha
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // viewModel.logoutUser() // 🔹 Se mantiene comentado
                            onLogout()
                        }
                )
            }

            // Barra de búsqueda
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(45.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Search",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryColorButtons(homeViewModel: HomeViewModel = viewModel()) {
    val categories = homeViewModel.categories

    val categoryColors = listOf(
        Color(0xffef635a), Color(0xfff59762), Color(0xff29d697),
        Color(0xff3b5998), Color(0xff8e44ad), Color(0xff2c3e50),
        Color(0xff16a085), Color(0xfff39c12)
    )

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val color = categoryColors[index % categoryColors.size]

                CategoryButton(
                    category = category,
                    color = color,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun CategoryButton(category: Category, color: Color, onClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color, RoundedCornerShape(16.dp))
            .size(138.dp, 41.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
    ) {
        Text(
            text = category.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}


@Composable
fun EventSliders(viewModel: HomeViewModel = viewModel()) {
    val upcomingEvents = viewModel.upcomingEvents
    val nearbyEvents = viewModel.nearbyEvents
    val recommendedEvents = viewModel.recommendedEvents

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 52.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            EventSection(title = "Upcoming Events", events = upcomingEvents)
        }
        item {
            EventSection(title = "Recommended Events", events = recommendedEvents)
        }
        item {
            EventSection(title = "Nearby Events", events = nearbyEvents)
        }
    }
}

@Composable
fun EventSection(title: String, events: List<Event>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(start = 19.dp)
        ) {
            Text(
                text = title,
                color = Color(0xff191d17),
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal
            )
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp)
        ) {
            items(events) { event ->
                EventBox(event)
            }
        }
    }
}

@Composable
fun EventBox(event: Event) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .size(237.dp, 188.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .advancedShadow(color = Color(0x3f000000), alpha = 0.25f, cornersRadius = 16.dp, shadowBlurRadius = 4.dp, offsetX = 0.dp, offsetY = 4.dp)
                .background(Color(0xfffffcfc), RoundedCornerShape(16.dp))
                .size(237.dp, 195.dp),
        )

        Image(
            painter = painterResource(id = R.drawable.mock_image),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 11.dp, y = 9.dp)
                .size(215.dp, 106.dp)
        )

        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 11.dp, y = 115.dp),
            text = event.title,
            color = Color(0xff191d17),
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Left,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 9.dp, y = 140.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Event Date",
                tint = Color(0xff838291),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = event.date,
                color = Color(0xff838291),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 9.dp, y = 160.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Event Location",
                tint = Color(0xff838291),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = event.location,
                color = Color(0xff838291),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewPreview() {
    GrowhubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainView(onLogout = {})
        }
    }
}

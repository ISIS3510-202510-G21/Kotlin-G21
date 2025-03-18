package com.isis3510.growhub.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.isis3510.growhub.utils.advancedShadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.isis3510.growhub.viewmodel.Event
import com.isis3510.growhub.view.BottomNavBar
import com.isis3510.growhub.view.navigation.AppNavHost

@Composable
fun MainView() {
    // This is the whole screen renderer
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .background(Color(0xffffffff))
            .size(412.dp, 917.dp)
            .clipToBounds(),
    ) {
        TopBoxRenderer()
        CategoryColorButtons()
        EventSliders()
    }
}

/*
@Composable
fun NavigationBar() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
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
                TopBoxRenderer()
                CategoryColorButtons()
                EventSliders()
            }
        }
    }
}
*/

@Composable
fun TopBoxRenderer(viewModel: HomeViewModel = viewModel()) {
    // This is the top thing renderer
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .offset(x = 0.dp, y = (-48).dp)
            .size(412.dp, 228.dp),
    ) {
        // This makes it be rounded at the bottom left and right
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xff4a43ec), RoundedCornerShape(36.dp))
                .size(412.dp, 228.dp),
        )
        // This is used to hide the upper bound
        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 0.dp, y = 75.dp)
                .size(394.dp, 55.dp),
        ) {
            // Component that holds the Current Location
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 158.dp, y = 14.dp)
                    .size(105.dp, 20.dp),
            ) {
                // Icon for arrow (Material UI)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 100.dp)
                        .size(16.dp, 16.dp),
                )
                // Text for Current Location
                Text(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .wrapContentSize(),
                    text = "Current Location",
                    color = Color(0xffffffff),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
                // Text for Bogota, Colombia
                Text(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .wrapContentSize()
                        .offset(x = 3.dp, y = 16.dp),
                    text = "Bogota, Colombia",
                    color = Color(0xffffffff),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // Icon for Account (Material UI)
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account Icon",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 350.dp, y = 16.dp)
                    .size(24.dp, 24.dp),
            )
        }
        // This renders the search bar embedded in the blue box
        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 22.dp, y = 140.dp)
                .size(259.dp, 45.dp),
        ) {
            // Icon for Search (Material UI)
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 9.dp, y = 10.dp)
                    .size(24.dp, 24.dp),
            )
            // This renders the Search text in the bar
            Text(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 35.479.dp, y = 13.dp)
                    .size(86.4.dp, 19.286.dp),
                text = "Search",
                color = Color(0xffffffff),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                overflow = TextOverflow.Ellipsis,
            )
            // This renders the Search field
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color(0x00d9d9d9), RoundedCornerShape(10.dp))
                    .size(259.dp, 45.dp)
                    .border(2.dp, Color(0xffffffff), RoundedCornerShape(10.dp)),
            )
        }
    }
}

@Composable
fun CategoryColorButtons() {
    // Rendering of the box that contains the slider of buttons for categories
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .offset(x = 11.dp, y = 194.dp)
            .size(438.dp, 41.dp),
    ) {
        // Column-3094:4119-Button
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xffef635a), RoundedCornerShape(16.dp))
                .size(138.dp, 41.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            // Row-I3094:4119;53923:27634-state-layer
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxSize()
                    .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                // Text-I3094:4119;53923:27635-label-text
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .wrapContentSize(),
                    text = "Programming",
                    color = Color(0xffffffff),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // Column-3094:4120-Button
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 150.dp, y = 0.dp)
                .background(Color(0xfff59762), RoundedCornerShape(16.dp))
                .size(138.dp, 41.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            // Row-I3094:4120;53923:27634-state-layer
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxSize()
                    .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                // Text-I3094:4120;53923:27635-label-text
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .wrapContentSize(),
                    text = "Entrepreneurship",
                    color = Color(0xffffffff),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // Column-3094:4121-Button
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 300.dp, y = 0.dp)
                .background(Color(0xff29d697), RoundedCornerShape(16.dp))
                .size(138.dp, 41.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            // Row-I3094:4121;53923:27634-state-layer
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxSize()
                    .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                // Text-I3094:4121;53923:27635-label-text
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .wrapContentSize(),
                    text = "UI/UX Design",
                    color = Color(0xffffffff),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun EventSliders(viewModel: HomeViewModel = viewModel()) {
    val upcomingEvents = viewModel.upcomingEvents
    val nearbyEvents = viewModel.nearbyEvents
    val recommendedEvents = viewModel.recommendedEvents

    LazyColumn(
        modifier = Modifier
            .offset(x = 19.dp, y = 250.dp)
            .fillMaxWidth()
            .padding(bottom = 20.dp), // Space at the end to avoid problem with scroll
        verticalArrangement = Arrangement.spacedBy(20.dp) // Space between sections
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
        Text(
            text = title,
            color = Color(0xff191d17),
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(15.dp)
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
            .offset(x = 5.dp, y = 0.dp)
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
            MainView()
        }
    }
}

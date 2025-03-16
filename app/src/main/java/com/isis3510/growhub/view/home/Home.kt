package com.isis3510.growhub.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import com.isis3510.growhub.utils.advancedShadow
import androidx.compose.ui.tooling.preview.Preview
import com.isis3510.growhub.viewmodel.Event

@Composable
fun MainView(viewModel: HomeViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Upcoming Events",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191D17),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow {
            items(viewModel.upcomingEvents) { event ->
                EventCard(event)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nearby Events",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191D17),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow {
            items(viewModel.nearbyEvents) { event ->
                EventCard(event)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You might like",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191D17),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.recommendedEvents) { event ->
                EventCard(event)
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFCFC))
            .advancedShadow(color = Color.Black, alpha = 0.25f, cornersRadius = 16.dp, shadowBlurRadius = 4.dp, offsetX = 0.dp, offsetY = 4.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.mock_image),
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191D17))
            Text(text = event.date, fontSize = 12.sp, color = Color(0xFF838291))
            Text(text = event.location, fontSize = 12.sp, color = Color(0xFF838291))
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

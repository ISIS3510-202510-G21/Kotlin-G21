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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.isis3510.growhub.utils.advancedShadow
import androidx.compose.ui.tooling.preview.Preview
import com.isis3510.growhub.viewmodel.Event

@Composable
fun MainView(viewModel: HomeViewModel = viewModel()) {
    TopBoxRenderer()
}

@Composable
fun TopBoxRenderer(viewModel: HomeViewModel = viewModel()) {
    // This is the whole screen renderer
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .background(Color(0xffffffff))
            .size(412.dp, 917.dp)
            .clipToBounds(),
    ) {
        // This is the top thing renderer
        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 0.dp, y = -48.dp)
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
                        .size(105.dp, 16.dp),
                ) {
                    // Here goes the arrow (REPLACE WITH MUI)
                    /*
                    Image(
                        painter = painterResource(id = R.drawable.image_30944103),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 97.dp, y = 6.dp)
                            .size(8.dp, 5.dp),
                    )
                    */
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
                    // Text for Bogota, Colombia (REPLACED WITH SENSOR)
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
                // Here goes the account symbol (REPLACE WITH MUI)
                /*
                // Image-3094:4107-account_circle
                Image(
                    painter = painterResource(id = R.drawable.image2_30944107),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 370.dp, y = 16.dp)
                        .size(24.dp, 24.dp),
                )
                */
            }
            // This renders the search bar embedded in the blue box
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 22.dp, y = 140.dp)
                    .size(259.dp, 45.dp),
            ) {
                // This is the search icon (REPLACED WITH MUI)
                /*
                // Image-3094:4109-search
                Image(
                    painter = painterResource(id = R.drawable.image_30944109),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 9.dp, y = 10.dp)
                        .size(24.dp, 24.dp),
                )*/
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

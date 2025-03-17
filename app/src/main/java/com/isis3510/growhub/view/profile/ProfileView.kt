package com.isis3510.growhub.view.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.R
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.viewmodel.ProfileViewModel
import com.isis3510.growhub.viewmodel.Profile

/**
 * Created by: Juan Manuel Jáuregui
 */

@Composable
fun MainView(viewModel: ProfileViewModel = viewModel()) {
    ProfileView(profile = Profile())
}

@Composable
fun ProfileView(profile: Profile?) {

    // Base Column Widget
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {

        // Space between the Top and Arrow
        Spacer(modifier = Modifier.height(32.dp))

        // Arrow Back
        Row (modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically){
            IconButton(
                onClick = { /* Handle back button click */ },
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // Profile Title
            Text(text = "Profile", fontSize = 24.sp)
        }

        // Space between Profile Title and Profile Image
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Image
        if (profile?.profilePictureUrl?.isNotEmpty() == true) {
            Image(
                painter = rememberAsyncImagePainter(profile.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_growhub), // Default profile image
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        }

        // Space between Profile Picture and Name
        Spacer(modifier = Modifier.height(32.dp))

        // Name
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = if (profile?.name.isNullOrEmpty()) "Loading..." else profile?.name!!,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Space between Name and Following / Followers
        Spacer(modifier = Modifier.height(32.dp))

        // Following / Followers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${profile?.following ?: 0}", fontWeight = FontWeight.Bold)
                Text(text = "Following")
            }
            VerticalDivider(
                color = Color.Gray,
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${profile?.followers ?: 0}", fontWeight = FontWeight.Bold)
                Text(text = "Followers")
            }
        }

        // Space between Following / Followers and Edit Profile Button
        Spacer(modifier = Modifier.height(32.dp))

        // Edit Profile Button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

            // Button
            OutlinedButton(onClick = { /* Handle edit profile */ }
                ,modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(48.dp)
                ,shape = RoundedCornerShape(16.dp)
                ,border = BorderStroke(2.dp, Color(0xFF5669FF))
                ,colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5669FF))) {

                // Row for Icon and Text
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_edit)
                        ,contentDescription = "Edit Profile"
                        ,tint = Color(0xFF5669FF))

                    // Space between Icon and Text
                    Spacer(modifier = Modifier.width(8.dp))

                    // Text for Edit Profile
                    Text(
                        text = "Edit Profile",
                        color = Color(0xFF5669FF),
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Space between Edit Profile and About Me
        Spacer(modifier = Modifier.height(32.dp))

        // About Me
        Text(text = "About Me", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Left)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = profile?.aboutMe ?: "No information available",
            textAlign = TextAlign.Justify
        )

        // Space between About Me and Interests
        Spacer(modifier = Modifier.height(16.dp))

        // Interests
        InterestsSection(profile?.interests ?: listOf())
    }
}

// Interests Section Function
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsSection(interests: List<String>) {
    val colors = listOf(
        Color(0xFF6B7AED),  // Violet
        Color(0xFFEE544A),  // Red
        Color(0xFFFF8D5D),  // Orange
        Color(0xFF7D67EE),  // Purple
        Color(0xFF29D697),  // Green
        Color(0xFF39D1F2)   // Blue
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Interests",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Left
            )

            OutlinedButton(
                onClick = { /* Handle change action */ },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(30.dp)
                    .width(80.dp),
                contentPadding = PaddingValues(0.dp),
                border = null,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF5669FF).copy(alpha = 0.1f),
                    contentColor = Color(0xFF5669FF)
                )
            ) {
                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF5669FF)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(text = "Change", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interests.forEachIndexed { index, interest ->
                val color = colors[index % colors.size] // Assign colors cyclically

                InterestChip(text = interest, backgroundColor = color)

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

// Interest Chip Function
@Composable
fun InterestChip(text: String, backgroundColor: Color) {

    // Chip Box
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {

        // Chip Text
        Text(text = text, color = Color.White, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileViewPreview() {
    GrowhubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainView()
        }
    }
}


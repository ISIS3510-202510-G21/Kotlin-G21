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
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.R
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.viewmodel.ProfileViewModel

/**
 * Created by: Juan Manuel Jáuregui
 */

@Composable
fun ProfileView(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    navController: NavController
) {
    Scaffold(
        topBar = { ProfileTopBar(onNavigateBack) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                BottomNavigationBar(navController = navController)
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ProfileContent(viewModel, onNavigateToEditProfile)
        }
    }
}

@Composable
fun ProfileContent(
    viewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit
) {
    val profileList = viewModel.profile
    if (profileList.isNotEmpty()) {
        val profile = profileList[0]
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            ProfileImage(profile.profilePicture)
            Spacer(modifier = Modifier.height(32.dp))
            ProfileName(profile.name)
            Spacer(modifier = Modifier.height(32.dp))
            ProfileStats(profile.following, profile.followers)
            Spacer(modifier = Modifier.height(32.dp))
            EditProfileButton(onNavigateToEditProfile)
            Spacer(modifier = Modifier.height(32.dp))
            ProfileAbout(profile.description)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInterestsSection(profile.interests, onNavigateToEditProfile)
        }
    }
}

@Composable
fun ProfileTopBar(onNavigateBack: () -> Unit = {}) {
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
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xff191d17),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun ProfileImage(profilePictureUrl: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (profilePictureUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_growhub),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ProfileName(name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(text = name.ifEmpty { "Loading..." },
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xff191d17))
    }
}

@Composable
fun ProfileStats(following: Int, followers: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$following",
                fontWeight = FontWeight.Bold,
                color = Color(0xff191d17))
            Text(text = "Following",
                color = Color(0xff191d17))
        }
        VerticalDivider(
            color = Color.Gray,
            modifier = Modifier.height(32.dp).width(1.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$followers",
                fontWeight = FontWeight.Bold,
                color = Color(0xff191d17))
            Text(text = "Followers",
                color = Color(0xff191d17))
        }
    }
}

@Composable
fun EditProfileButton(onNavigateToEditProfile: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        OutlinedButton(
            onClick = onNavigateToEditProfile,
            modifier = Modifier.fillMaxWidth(0.5f).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Color(0xFF5669FF)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5669FF))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit Profile",
                    tint = Color(0xFF5669FF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Edit Profile", color = Color(0xFF5669FF), fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileAbout(aboutMe: String) {
    Text(text = "About Me",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        textAlign = TextAlign.Left,
        color = Color(0xff191d17))
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = aboutMe,
        textAlign = TextAlign.Justify,
        color = Color(0xff191d17))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileInterestsSection(interests: List<String>, onNavigateToEditProfile: () -> Unit) {
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
                fontSize = 20.sp,
                textAlign = TextAlign.Left,
                color = Color(0xff191d17)
            )

            OutlinedButton(
                onClick = { onNavigateToEditProfile() },
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

                ProfileInterestChip(text = interest, backgroundColor = color)

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun ProfileInterestChip(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {

        Text(text = text, color = Color.White, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileViewPreview() {

    val navController = rememberNavController()

    GrowhubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileView(navController = navController, onNavigateBack = {}, onNavigateToEditProfile = {})
        }
    }
}


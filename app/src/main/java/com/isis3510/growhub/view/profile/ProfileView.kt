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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.isis3510.growhub.R
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.view.navigation.BottomNavigationBar
import com.isis3510.growhub.viewmodel.ConnectivityViewModel
import com.isis3510.growhub.viewmodel.ProfileViewModel

/**
 * Created by: Juan Manuel Jáuregui
 */

@Composable
fun ProfileView(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    connectivityViewModel: ConnectivityViewModel = viewModel(),
    navController: NavController
) {

    val currentStatus by connectivityViewModel.networkStatus.collectAsState()
    val initialNetworkAvailable = remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {

        if (initialNetworkAvailable.value == null) {
            initialNetworkAvailable.value = currentStatus == ConnectionStatus.Available
        }
    }

    val profileList = viewModel.profile

    Scaffold(
        topBar = { ProfileTopBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                profileList.isNotEmpty() -> {
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
                initialNetworkAvailable.value == false -> {
                    ProfileEmpty()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 50.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar(navController = navController)
        }
    }
}

@Composable
fun ProfileTopBar(onNavigateBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Following",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        VerticalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.height(32.dp).width(1.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$followers",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Followers",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Edit Profile", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
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
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = aboutMe,
        textAlign = TextAlign.Justify,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun ProfileEmpty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No Internet Connection",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're offline",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please check your internet connection and try again.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}



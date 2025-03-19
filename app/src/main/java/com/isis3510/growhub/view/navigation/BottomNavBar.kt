package com.isis3510.growhub.view.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isis3510.growhub.R


// Bottom Navigation Bar
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navItems = listOf(
        Destinations.HOME to "Home",
        Destinations.MAP to "Map",
        "Add" to "Add",
        Destinations.MY_EVENTS to "My Events",
        Destinations.PROFILE to "Profile"
    )
    var selectedItem by remember { mutableStateOf(Destinations.HOME) }


    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        navItems.forEach { (route, label) ->
            NavigationBarItem(
                icon = {
                    if (route == "Add") {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5669FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(
                                id = when (route) {
                                    Destinations.HOME -> R.drawable.home
                                    Destinations.MAP -> R.drawable.map
                                    Destinations.MY_EVENTS -> R.drawable.events
                                    Destinations.PROFILE -> R.drawable.profile
                                    else -> R.drawable.home
                                }
                            ),
                            contentDescription = label,
                            tint = if (selectedItem == route) Color(0xFF5669FF) else Color(0xFFDCD9D9),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                label = {
                    if (route != "Add") {
                        Text(
                            text = label,
                            color = if (selectedItem == route) Color(0xFF5669FF) else Color(0xFFDCD9D9)
                        )
                    }
                },
                selected = selectedItem == route,
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White),
                onClick = {
                    if (route == "Add") {
                        navController.navigate(Destinations.CREATE)
                    } else {
                        selectedItem = route
                        navController.navigate(route) {
                            popUpTo(Destinations.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
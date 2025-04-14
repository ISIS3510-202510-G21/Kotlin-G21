package com.isis3510.growhub.view.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isis3510.growhub.R
import com.isis3510.growhub.offline.NetworkUtils

@Composable
fun BottomNavigationBar(navController: NavController) {
    // Revisamos si hay internet
    val context = LocalContext.current
    val hasInternet = NetworkUtils.isNetworkAvailable(context)

    val navItems = listOf(
        Destinations.HOME to "Home",
        Destinations.MAP to "Map",
        "Add" to "Add",
        Destinations.MY_EVENTS to "My Events",
        Destinations.PROFILE to "Profile"
    )

    val navBackStackEntry = navController.currentBackStackEntryFlow.collectAsState(
        initial = navController.currentBackStackEntry
    )
    val currentRoute = navBackStackEntry.value?.destination?.route

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
                            tint = if (currentRoute == route) Color(0xFF5669FF) else Color(0xFFDCD9D9),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                label = {
                    if (route != "Add") {
                        Text(
                            text = label,
                            color = if (currentRoute == route) Color(0xFF5669FF) else Color(0xFFDCD9D9)
                        )
                    }
                },
                selected = currentRoute == route,
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White),

                // No bloquear directamente, siempre permitimos el click
                enabled = true,

                onClick = {
                    if (!hasInternet) {
                        Toast.makeText(context, "Please check your internet connection", Toast.LENGTH_SHORT).show()
                    } else {
                        if (route == "Add") {
                            navController.navigate(Destinations.CREATE)
                        } else {
                            navController.navigate(route) {
                                popUpTo(Destinations.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    }
}

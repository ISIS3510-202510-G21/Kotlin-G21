package com.isis3510.growhub.view.navigation // Ensure this package is correct

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State // Import State if using derivedStateOf explicitly
import androidx.compose.runtime.derivedStateOf // Import derivedStateOf
import androidx.compose.runtime.getValue // Import getValue extension
import androidx.compose.runtime.remember // Import remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // Keep for transparent color if needed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Use lifecycle-aware collect
import androidx.navigation.NavController
import androidx.navigation.NavDestination // Import NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination // Import for popUpTo logic
import com.isis3510.growhub.R

// List of Navigation Links and Icons --> Button uses Home button as placeholder
private val bottomNavItems = listOf(
    BottomNavItemConfig(Destinations.HOME, "Home", R.drawable.home),
    BottomNavItemConfig(Destinations.MAP, "Map", R.drawable.map),
    BottomNavItemConfig("add_special_button_route", "Add", R.drawable.home, isAddButton = true),
    BottomNavItemConfig(Destinations.MY_EVENTS, "My Events", R.drawable.events),
    BottomNavItemConfig(Destinations.PROFILE, "Profile", R.drawable.profile)
)

// Helper data class
private data class BottomNavItemConfig(
    val route: String,
    val label: String,
    val iconResId: Int,
    val isAddButton: Boolean = false
)

@Composable
fun BottomNavigationBar(navController: NavController) {

    // Collect current destination with lifecycle awareness
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    val currentDestination: NavDestination? = currentBackStackEntry?.destination

    // Use derivedStateOf to only recalculate the route string when the destination changes
    val currentRoute: String? = remember(currentDestination) {
        currentDestination?.route
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            if (item.isAddButton) {
                NavigationBarItem(
                    selected = false, // Add button is never visually selected
                    onClick = {
                        // Navigate to the specific Create destination from the graph
                        navController.navigate(Destinations.CREATE) {
                        Log.d("Navigation Tapped", item.route)
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    label = null, // No visible label
                    alwaysShowLabel = false
                )
            } else {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text(item.label) },
                    selected = isSelected,
                    onClick = {
                        // Avoid navigating if already on the destination
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) { // Navigate to the item's defined route
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        Log.d("Navigation Tapped", item.label)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    }
}
package com.isis3510.growhub.view.navigation

import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.isis3510.growhub.R
import com.isis3510.growhub.offline.NetworkUtils

/* --------------------------- config helpers --------------------------- */

private data class BottomNavItemConfig(
    val route: String,
    val label: String,
    val iconResId: Int,
    val isAddButton: Boolean = false
)

private val bottomNavItems = listOf(
    BottomNavItemConfig(Destinations.HOME, "Home", R.drawable.home),
    BottomNavItemConfig(Destinations.MAP, "Map", R.drawable.map),
    BottomNavItemConfig(Destinations.CREATE, "Add", R.drawable.home, isAddButton = true),
    BottomNavItemConfig(Destinations.MY_EVENTS, "My Events", R.drawable.events),
    BottomNavItemConfig(Destinations.PROFILE, "Profile", R.drawable.profile)
)

/* --------------------------- composable --------------------------- */

@Composable
fun BottomNavigationBar(navController: NavController) {

    /* ­Internet availability (kept from HEAD) */
    val context = LocalContext.current
    val hasInternet = NetworkUtils.isNetworkAvailable(context)

    /* ­Current destination (from develop) */
    val currentBackStackEntry by navController.currentBackStackEntryFlow
        .collectAsStateWithLifecycle(null)
    val currentDestination: NavDestination? = currentBackStackEntry?.destination
    val currentRoute: String? = remember(currentDestination) { currentDestination?.route }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            if (item.isAddButton) {
                /* --------------- floating “Add” button --------------- */
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        if (!hasInternet) {
                            Toast.makeText(
                                context,
                                "Please check your internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            navController.navigate(Destinations.CREATE)
                            Log.d("Navigation", "Add button tapped")
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
                    label = null,
                    alwaysShowLabel = false
                )
            } else {
                /* ------------------- regular items ------------------- */
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
                        if (!hasInternet) {
                            Toast.makeText(
                                context,
                                "Please check your internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@NavigationBarItem
                        }

                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            Log.d("Navigation", "${item.label} tapped")
                        }
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

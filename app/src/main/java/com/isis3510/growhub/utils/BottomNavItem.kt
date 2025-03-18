package com.isis3510.growhub.utils

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.ui.graphics.vector.ImageVector
import com.isis3510.growhub.R

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconVector: ImageVector?,
    @DrawableRes val iconRes: Int?,
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home, null)
    object Map : BottomNavItem("map", "Map", null, R.drawable.map)
    object New : BottomNavItem("new", "", null, null)
    object MyEvents : BottomNavItem("myevents", "My Events", null, R.drawable.events)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.AccountBox, null)
}

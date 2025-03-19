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
import com.isis3510.growhub.R


// Bottom Navigation Bar
@Composable
fun BottomNavigationBar() {
    val navItems = listOf("Home", "Map", "Add", "My Events", "Profile")
    var selectedItem by remember { mutableStateOf("Home") }

    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item == "Add") {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5669FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(
                                id = when (item) {
                                    "Home" -> R.drawable.home
                                    "Map" -> R.drawable.map
                                    "My Events" -> R.drawable.events
                                    "Profile" -> R.drawable.profile
                                    else -> R.drawable.home
                                }
                            ),
                            contentDescription = item,
                            tint = if (selectedItem == item) Color(0xFF5669FF) else Color(0xFFDCD9D9),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                label = {
                    if (item != "Add") {
                        Text(
                            text = item,
                            color = if (selectedItem == item) Color(0xFF5669FF) else Color(0xFFDCD9D9)
                        )
                    }
                },
                selected = selectedItem == item,
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White),
                onClick = { selectedItem = item }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun Preview() {
    BottomNavigationBar()
}
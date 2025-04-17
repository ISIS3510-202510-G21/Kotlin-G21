package com.isis3510.growhub.view.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isis3510.growhub.viewmodel.AuthViewModel
// Import the LocationViewModel and its state
import com.isis3510.growhub.viewmodel.LocationViewModel
import com.isis3510.growhub.viewmodel.SimpleLocationUiState
// Import Android's basic JSON tools
import org.json.JSONObject
import org.json.JSONException

@Composable
fun TopBarView(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    onLogout: () -> Unit,
    onSearchClick: () -> Unit
) {
    // Collect the simplified state type
    val locationState by locationViewModel.locationState.collectAsStateWithLifecycle()

    fun parseLocationFromJson(jsonString: String): String {
        if (jsonString.isBlank()) return "Invalid Response" // Handle empty string case
        return try {
            val jsonObject = JSONObject(jsonString) // Use Android's basic JSONObject
            when (jsonObject.optString("status")) {
                "success" -> {
                    val city = jsonObject.optString("city", "") // Default to empty if missing
                    val country = jsonObject.optString("country", "")
                    // Construct the display string, handling cases where one might be missing
                    listOfNotNull(city.takeIf { it.isNotEmpty() }, country.takeIf { it.isNotEmpty() })
                        .joinToString(", ")
                        .ifEmpty { "Location Data Missing" } // If both are empty
                }
                "fail" -> {
                    // API specifically returned a failure status
                    "Location unavailable: ${jsonObject.optString("message", "API failed")}"
                }
                else -> {
                    // Unexpected status or missing status field
                    "Unknown Location Status"
                }
            }
        } catch (e: JSONException) {
            // This happens if the string is not valid JSON
            Log.e("TopBarView", "Failed to parse location JSON: $jsonString", e)
            "Parsing Error"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Color(0xff4a43ec)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Row: Location and Logout ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Spacer to push location to center, Icon to end
                Spacer(modifier = Modifier.weight(1f))

                // Location Display Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(2f) // Give location text more horizontal space
                ) {
                    Text(
                        text = "Current Location",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Display Location based on the SimpleLocationUiState
                    when (locationState) {
                        is SimpleLocationUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp).padding(top = 2.dp), // Added padding
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                        is SimpleLocationUiState.Success -> {
                            // Parse the raw string before displaying
                            val displayLocation = parseLocationFromJson(
                                (locationState as SimpleLocationUiState.Success).rawResponse
                            )
                            Text(
                                text = displayLocation,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1, // Prevent wrapping if text is long
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Add ellipsis if too long
                            )
                        }
                        is SimpleLocationUiState.Error -> {
                            // Display a user-friendly error message
                            Text(
                                text = "Location unavailable",
                                // Consider showing details from: (locationState as SimpleLocationUiState.Error).message on tap/debug
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        is SimpleLocationUiState.Idle -> {
                            // Initial state before loading begins
                            Text(
                                text = "Fetching...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Logout Icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier
                        .weight(1f) // Ensure it takes up space to balance the row
                        .wrapContentWidth(Alignment.End) // Align the icon itself to the far right
                        .size(24.dp)
                        .clickable {
                            authViewModel.logoutUser() // Call logout on the provided ViewModel
                            onLogout() // Trigger the navigation/action callback
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space between top row and search bar

            // --- Search Bar ---
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f) // Slightly wider search bar
                    .height(45.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp)) // Subtle border
                    .clickable { onSearchClick() }, // Trigger search callback
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Events or Categories...", // Placeholder text
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
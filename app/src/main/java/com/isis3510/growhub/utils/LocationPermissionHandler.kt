package com.isis3510.growhub.utils

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.isis3510.growhub.viewmodel.LocationViewModel

@Composable
fun LocationPermissionHandler(
    locationViewModel: LocationViewModel,
    onPermissionResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        onPermissionResult(hasLocationPermission)

        if (hasLocationPermission) {
            // Refrescar ubicación con GPS cuando se concedan los permisos
            locationViewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            hasRequestedPermission = true
        }
    }
}
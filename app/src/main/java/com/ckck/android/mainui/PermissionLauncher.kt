package com.ckck.android.mainui

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ckck.android.viewmodels.MainViewModel

class PermissionHandler(
    private val launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    fun requestPermission() {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

@Composable
fun rememberPermissionHandler(viewModel: MainViewModel): PermissionHandler {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkLocationAccessInternal(context, viewModel)
        } else {
            viewModel.showMissingPermissionAlert("Location access")
        }
    }

    return remember(context, viewModel, launcher) {
        PermissionHandler(launcher)
    }
}

private fun checkLocationAccessInternal(context: Context, viewModel: MainViewModel) {
    val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    if (!locationManager.isLocationEnabled) {
        viewModel.showMissingPermissionAlert("Location services")
    } else {
        viewModel.getCurrentLocations()
    }
}

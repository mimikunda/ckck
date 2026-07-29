package com.ckck.android.mainui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.ckck.android.viewmodels.LocationError
import com.ckck.android.viewmodels.MainViewModel

class PermissionHandler(
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    fun requestPermission() {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
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
            val activity = context as? Activity
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: true

            if (!shouldShowRationale) {
                viewModel.showLocationError(LocationError.PermanentlyDenied)
            } else {
                viewModel.showLocationError(LocationError.MissingPermission)
            }
        }
    }

    return remember(context, viewModel, launcher) {
        PermissionHandler(context, launcher)
    }
}

private fun checkLocationAccessInternal(context: Context, viewModel: MainViewModel) {
    val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    if (!locationManager.isLocationEnabled) {
        viewModel.showLocationError(LocationError.LocationDisabled)
    } else {
        viewModel.getCurrentLocations()
    }
}

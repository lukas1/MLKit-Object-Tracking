package com.garwan.mlkitjetpackcompose.ui.screen

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.garwan.mlkitjetpackcompose.ui.screen.views.ObjectTracking
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        ObjectTracking()
    } else {
        LaunchedEffect(key1 = "permission_request", block = {
            cameraPermissionState.launchPermissionRequest()
        })

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Please grant permissions to use the camera")
            Button(onClick = cameraPermissionState::launchPermissionRequest) {
                Text(text = "Grant permission")
            }
        }
    }
}
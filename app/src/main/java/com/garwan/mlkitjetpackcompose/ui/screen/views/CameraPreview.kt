package com.garwan.mlkitjetpackcompose.ui.screen.views

import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(cameraController: CameraController) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = cameraController
            }
        }
    )
}
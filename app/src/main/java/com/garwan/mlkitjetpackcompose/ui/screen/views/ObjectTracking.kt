package com.garwan.mlkitjetpackcompose.ui.screen.views

import android.util.Size
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.garwan.mlkitjetpackcompose.ui.screen.object_tracking.ObjectTrackingImageAnalyzer
import com.garwan.mlkitjetpackcompose.ui.screen.object_tracking.customObjectDetector
import com.google.mlkit.vision.objects.DetectedObject

@Composable
fun ObjectTracking() {
    var analyzedImageSize by remember {
        mutableStateOf(Size(0, 0))
    }
    var detectedObjects by remember {
        mutableStateOf(listOf<DetectedObject>())
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                ObjectTrackingImageAnalyzer(
                    objectDetector = customObjectDetector()
                ) { imageSize, objects ->
                    analyzedImageSize = imageSize
                    detectedObjects = objects
                }
            )
        }
    }

    CameraPreview(cameraController)

    ObjectTrackingOverlay(
        detectedObjects = detectedObjects,
        imageSize = analyzedImageSize
    )
}
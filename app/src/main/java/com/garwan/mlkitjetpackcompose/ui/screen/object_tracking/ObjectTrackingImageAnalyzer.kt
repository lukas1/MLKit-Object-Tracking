package com.garwan.mlkitjetpackcompose.ui.screen.object_tracking

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector

class ObjectTrackingImageAnalyzer(
    private val objectDetector: ObjectDetector,
    val onDetectorSuccess: (imageSize: Size, detectedObjects: List<DetectedObject>) -> Unit
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            objectDetector.process(
                InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )
            )
                .addOnSuccessListener { objects ->
                    onDetectorSuccess(
                        Size(image.width, image.height),
                        objects
                    )
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
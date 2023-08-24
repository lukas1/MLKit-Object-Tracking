package com.garwan.mlkitjetpackcompose.ui.screen.object_tracking

import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions

fun customObjectDetector(): ObjectDetector {
    val localModel = LocalModel.Builder()
        .setAssetFilePath("mnasnet_1.3_224_1_metadata_1.tflite")
        .build()

    return ObjectDetection.getClient(
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .setClassificationConfidenceThreshold(0.99f)
            .enableClassification()
            .build()
    )
}
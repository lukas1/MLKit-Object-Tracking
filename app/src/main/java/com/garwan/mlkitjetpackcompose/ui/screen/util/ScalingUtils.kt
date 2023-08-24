package com.garwan.mlkitjetpackcompose.ui.screen.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.max

private fun calculateScaledSizeOfImage(canvasSize: Size, imageSize: android.util.Size): Size {
    val widthRatio = canvasSize.width / imageSize.width
    val heightRatio = canvasSize.height / imageSize.height
    val bestRatio = max(widthRatio, heightRatio)

    return Size(
        width = imageSize.width * bestRatio,
        height = imageSize.height * bestRatio
    )
}

private fun calculateCroppedWidth(canvasSize: Size, imageSize: android.util.Size): Float {
    val scaledWidth = calculateScaledSizeOfImage(canvasSize, imageSize).width
    return scaledWidth - canvasSize.width
}

private fun calculateCroppedHeight(canvasSize: Size, imageSize: android.util.Size): Float {
    val scaledHeight = calculateScaledSizeOfImage(canvasSize, imageSize).height
    return scaledHeight - canvasSize.height
}

fun Rect.adjustedFromImageToCanvas(imageSize: android.util.Size, canvasSize: Size): Rect {
    val croppedWidth = calculateCroppedWidth(canvasSize, imageSize)
    val croppedHeight = calculateCroppedHeight(canvasSize, imageSize)

    return Rect(
        offset = Offset(
            x = (topLeft.x / imageSize.width * canvasSize.width) - (croppedWidth / 4),
            y = (topLeft.y / imageSize.height * canvasSize.height) - (croppedHeight / 4)
        ),
        size = Size(
            width = (width / imageSize.width * canvasSize.width) + (croppedWidth / 2),
            height = (height / imageSize.height * canvasSize.height) + (croppedHeight / 2)
        )
    )
}
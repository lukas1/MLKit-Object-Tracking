package com.garwan.mlkitjetpackcompose.ui.screen.views

import android.util.Size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.garwan.mlkitjetpackcompose.ui.screen.util.adjustedFromImageToCanvas
import com.google.mlkit.vision.objects.DetectedObject

@Composable
fun ObjectTrackingOverlay(
    detectedObjects: List<DetectedObject>,
    imageSize: Size
) {
    val textMeasurer = rememberTextMeasurer()
    Spacer(
        modifier = Modifier
            .drawBehind {
                detectedObjects
                    .forEach { detectedObject ->
                        drawDetectedObjectRect(detectedObject, textMeasurer, imageSize)
                    }
            }
    )
}

private fun DrawScope.drawDetectedObjectRect(
    detectedObject: DetectedObject,
    textMeasurer: TextMeasurer,
    imageSize: Size
) {
    val detectedObjectBoundingBox = detectedObject.boundingBox
        .toComposeRect()
        .adjustedFromImageToCanvas(imageSize, this.size)

    drawRect(
        color = Color.Red,
        topLeft = detectedObjectBoundingBox.topLeft,
        size = detectedObjectBoundingBox.size,
        style = Stroke(width = 5.0f)
    )

    val measuredText = textMeasurer.measure(
        text = AnnotatedString(
            detectedObject.labels.firstOrNull()?.text
                ?: "UNKNOWN"
        ),
        style = TextStyle(
            fontSize = 18.sp,
            color = Color.Red
        )
    )

    val textHalfWidth = measuredText.size.width / 2
    val textHalfHeight = measuredText.size.height / 2

    drawText(
        textLayoutResult = measuredText,
        topLeft = Offset(
            x = detectedObjectBoundingBox.center.x - textHalfWidth,
            y = detectedObjectBoundingBox.center.y - textHalfHeight
        )
    )
}
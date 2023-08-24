# Step 5

In previous steps we were able to track objects captured by the device's camera on the screen and even classify them. Well, kind of. The rectangle that we've drawn on the screen was positioned incorrectly and it had the wrong size. Here we'll try to fix that.

## Why is the rectangle wrong?

You may remember, that in order to draw the rectangle, we have used the rectangle returned by the MLKit library, that in turn got that information from the pretrained ML model. So why is the rectangle wrong?

Well that's because the rectangle coordinates returned are relative to the image used in the analysis. Not relative to the view, that displays the camera preview. If you think about it, what the camera sees is not the same thing as it can display on the screen, since the camera preview takes up the whole screen. Since the screen has a different width and height ratio compared to the captured image ratio, it is obvious that some scaling must apply to the preview view. Indeed it is the `FILL_CENTER` scaling mode, that is used.

`FILL_CENTER` scaling is a scaling mode, where the image is scaled keeping the aspect ratio of the image, but so that both the height and the width of the canvas on which we scale are filled. Inevitably that also leads to some cropping. However, the coordinates of the `Rect` returned by `ObjectDetector` do not take this cropping into consideration. Its coordinates are relative to the uncropped image.

Not only that. The image analyzer actually doesn't work with a full sized image. That would be way too resource exhaustive and slow. Therefore, the image that is returned by the `ImageAnalysis` is downscaled to 480x640 pixels. Not only that, but the CameraX library actually also rotates the image (sometimes) based on the device orientation. Actually, interestingly enough, the library at least compensates for the rotation. How?

Recall the code that we used to pass the captured image to the `ObjectDetector`:

```kotlin
objectDetector.process(
    InputImage.fromMediaImage(
        image,
        imageProxy.imageInfo.rotationDegrees
    )
)
```

Notice the `imageProxy.imageInfo.rotationDegrees` line. That line there is precisely to compensate for the rotation of the image. Therefore the `ObjectDetector` already works with a unified orientation and the `Rect` that it returns is already properly positioned with regards to the analysed image. Unfortunately that doesn't entirely frees us from not having to care about rotation though.

As you may have noticed, we do not pass any information about the camera preview view to the library and there is no method in the `MLKit` or `CameraX` to compensate for cropping or scaling of the preview automatically for us. We have to do it manually. 

## The plan

Since we already understand the problem, it is clear, what we have to do. 

* Compensate for image rotation
* Get the analyzed image size
* Get the preview view size
* Calculate how large does the analyzed image appear on the preview view
* How much of the image is cropped
* Upscale and position the `Rect` returned by the `ObjectDetector`, compensating for the scale and crop

The plan is clear, so let's get to the tasks.

## Compensate for image rotation

The `Rect` returned by the `ObjectDetector` is already compensated for rotation. Unfortunately the image size returned by the CameraX library is not. It will always return the same size regardless of the orientation. This is bad, because then we'd be calculating incorrect scaled size of the image. To make things easier down the line, we'll adjust the image size based on the rotation degrees already in the `ObjectTrackingImageAnalyzer` and pass the corrected image size to the `onDetectorSuccess` callback. First, let's create an extension method in the `ObjectTrackingImageAnalyzer` file.

```kotlin
private fun Size.rotationAdjustedImageSize(imageRotationDegrees: Int): Size {
    val rotationAdjustedWidth = if (imageRotationDegrees == 90 || imageRotationDegrees == 270) height else width
    val rotationAdjustedHeight = if (imageRotationDegrees == 90 || imageRotationDegrees == 270) width else height

    return Size(
        rotationAdjustedWidth,
        rotationAdjustedHeight
    )
}
```

This method swaps height and width if the image is rotated vertically.

Now, call this method before passing the size to the `onDetectorSuccess` in the `ObjectTrackingImageAnalyzer`:

```kotlin
onDetectorSuccess(
    Size(image.width, image.height)
        .rotationAdjustedImageSize(
            imageProxy.imageInfo.rotationDegrees
        ),
    objects
)
```

## Get the analyzed image size

While we have mentioned that the analyzed image size is 480*640 pixels, and we could definitely hard code this value (in fact some demos online do just that), it is also not the best approach, because these numbers could change in the future. It is better to get the actual image size from the CameraX library. There's one method, where we have all the information about the image. It is the `ImageAnalyzer.Analysis::analyze` method. If you recall, we pass the image size from this method to the `onDetectorSuccess`:

```kotlin
onDetectorSuccess(
    Size(image.width, image.height),
    objects
)
```

However, we don't store this information anywhere afterwards. So let's store this information within the `ObjectTracking` composable, so that we can pass it later to the `ObjectTrackingOverlay` composable, where it can be used to position rectangle properly.

```kotlin
var analyzedImageSize by remember {
    mutableStateOf(Size(0, 0))
}
```

and fill this state from the `onDetectorSuccess` callback, in the `ObjectTracking`:

```kotlin
setImageAnalysisAnalyzer(
    ContextCompat.getMainExecutor(context),
    ObjectTrackingImageAnalyzer(
        objectDetector = customObjectDetector()
    ) { imageSize, objects ->
        analyzedImageSize = imageSize
        detectedObjects = objects
    }
)
```

pass this information down to the `DrawScope::drawDetectedObjectRect` method in the `ObjectTrackingOverlay` file.

```kotlin
private fun DrawScope.drawDetectedObjectRect(
    detectedObject: DetectedObject,
    textMeasurer: TextMeasurer,
    imageSize: Size
) {
    ...
}
```

## Get the preview view size

This is the easy part. `DrawScope` has a property called `size`, where we can access the canvas size.

## Calculate how large does the image appear on the preview view

Combining all the information that we already have, it should be relatively easy to construct a method, that can calculate the size of the scaled and cropped image in the preview view. We'll create a new file called `ScalingUtils`, where we can create the following method:

```kotlin
private fun calculateScaledSizeOfImage(canvasSize: Size, imageSize: android.util.Size): Size {
    val widthRatio = canvasSize.width / imageSize.width
    val heightRatio = canvasSize.height / imageSize.height
    val bestRatio = max(widthRatio, heightRatio)

    return Size(
        width = imageSize.width * bestRatio,
        height = imageSize.height * bestRatio
    )
}
```

This method has two parameters of two different `Size` types. The first parameter represents the canvas size and the type `Size` comes from the Jetpack Compose library. We can get this size from the `DrawScope`. The second parameter is the size of the analyzed image returned from the `ObjectDetector`. We calculate the ratio between widths of the canvas and the image and calculate the same for heights. We choose the ratio that guarantees that the image fills the screen. 

## How much of the image is cropped

It's now also easy to calculate how much of the image is cropped. We compare the canvas size to the size of the upscaled image. Here we create two methods in the `ScalingUtils` that calculate how much is the image cropped horizontally and vertically:

```kotlin
private fun calculateCroppedWidth(canvasSize: Size, imageSize: android.util.Size): Float {
    val scaledWidth = calculateScaledSizeOfImage(canvasSize, imageSize).width
    return scaledWidth - canvasSize.width
}

private fun calculateCroppedHeight(canvasSize: Size, imageSize: android.util.Size): Float {
    val scaledHeight = calculateScaledSizeOfImage(canvasSize, imageSize).height
    return scaledHeight - canvasSize.height
}
```

* Upscale and position the `Rect` returned by the `ObjectDetector`, compensating for the scale and crop

Now we can upscale the `Rect` returned by the `ObjectDetector`. We'll turn to extension methods here again, for bit nicer method call signature. Let's add a new method to the `ScalingUtils` file:

```kotlin
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
```

Here we adjust the offset of where the rectangle appears on the screen. For that we get the ratio between the image size and the coordinate and multiply it by the dimensions of the canvas. We also compensate for the cropping, so we need to move square by about one fourth of the cropped size up and left on the screen. Similarly we calculate the new rectangle size, this time we have to add half of the cropped size to each dimension of the rectangle.

Now we just call this method inside the `ObjectTrackingOverlay`, in the `DrawScope::drawDetectedObjectRect` method and we're finally done.

```kotlin
val detectedObjectBoundingBox = detectedObject.boundingBox
    .toComposeRect()
    .adjustedFromImageToCanvas(imageSize, this.size)
```

Now the rest of the method uses the correct size of the detected object rectangle and the demo works as intended, regardless of screen orientation.

## Conclusion

And with that, we're done. We have used Jetpack Compose together with CameraX, MLKit and a custom pretrained TensorFlow model to create an app that tracks and classifies the most prominent object captured by the camera on the fly. Have fun with the demo!
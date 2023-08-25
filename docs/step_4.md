# Step 4

In previous steps we have prepared the camera preview and MLKit object detector. In this step we'll finally draw some rectangles on the screen, representing an object tracked by the MLKit from the camera feed. 

## Attaching the analyzer to CameraController

Okay, we have an image analyzer, now we need to connect it to the camera controller. Let's modify the code of our `ObjectTracking` composable, where we're creating the `CameraController`. We can attach the image analyzer there:

```kotlin
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                ObjectTrackingImageAnalyzer(
                    objectDetector = customObjectDetector()
                ) { imageSize, detectedObjects ->
                    // TODO: Store the state
                }
            )
        }
    }
```

Basically we have added just one call on the cameraController, to `setImageAnalysisAnalyzer`. Here we also provide the callback in which we can store the detectedObjects, so that we can draw them. Let's do that. In our `ObjectTracking` composable we'll add a new property:

```kotlin
var detectedObjects by remember {
    mutableStateOf(listOf<DetectedObject>())
}
```

and set the value from the callback. Please note that due to name shadowing we've renamed the parameter of the `onDetectorSuccess` callback:

```kotlin
ObjectTrackingImageAnalyzer(
    objectDetector = customObjectDetector()
) { imageSize, objects ->
    detectedObjects = objects
}
```

## Drawing tracked object on the screen

Now that we have the detector finally attached to the controller and we can get a list of tracked objects out of it, we can finally draw the rectangle tracking the object on screen. For that, we're going to create first a new file with a new composable, called `ObjectTrackingOverlay`. We can add a following composable there:

```kotlin
@Composable
fun ObjectTrackingOverlay(
    detectedObjects: List<DetectedObject>
) {
    Spacer(
        modifier = Modifier
            .drawBehind {
                detectedObjects
                    .forEach { detectedObject ->
                        drawDetectedObjectRect(detectedObject)
                    }
            }
    )
}

private fun DrawScope.drawDetectedObjectRect(
    detectedObject: DetectedObject
) {
    val detectedObjectBoundingBox = detectedObject.boundingBox
        .toComposeRect()

    drawRect(
        color = Color.Red,
        topLeft = detectedObjectBoundingBox.topLeft,
        size = detectedObjectBoundingBox.size,
        style = Stroke(width = 5.0f)
    )
}
```

There's a lot to unpack here, so let's take a look. First thing to notice is, that we're executing most of the code within the `drawBehind` lambda. This is Jetpack Compose API that enables us to draw on screen as if on a canvas and we're free to draw any shapes we like. We're using it to draw rectangles.

Next thing to note is, that we loop through all the detected objects and call an extension method `drawDetectedObjectRect` on each detected object. This is just to keep the code more organised. The important part happens in the extension method `drawDetectedObjectRect`. As mentioned before, while this code does support multiple objects being tracked, in practice the model that we're using tracks only the most prominent object on the screen. As such, expect at most one rectangle on the screen to appear.

In the `drawDetectedObjectRect` we access the `boundingBox` of the detected object. Since the MLKit APIs are still catered to the old Android View system, we use Jetpack Compose's `toComposeRect()` extension method, to convert it to be used in the context of Jetpack Compose. Finally, we can draw a rectangle, using `drawRect` method of the `DrawScope`.

## Adding a label

We'd also like to see a label, naming the tracked object. Please note, that the model that we're using is not very accurate when classifying objects. Therefore it will classify objects incorrectly quite often. This is the limitation of the model used. With better model, we'd also get better labels. Needless to say such models are also much larger and much slower to execute. Nevertheless, let's add code to display the label. In this case we want the text to be neatly centered in the rectangle representing the tracked object. For that we can use Jetpack Compose's TextMeasurer API. First we create an instance of the `TextMeasurer` in the `ObjectTrackingOverlay` composable:

```kotlin
val textMeasurer = rememberTextMeasurer()
```

We can then pass this measurer to the `drawDetectedObjectRect` as a parameter. There, we can use it, to measure the size of the text:

```kotlin
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
```

We're using the `TextMeasurer::measure` method to measure the size of the text. Knowing the size of the text, it is easy to properly locate it in the center of the rectangle and draw it onto the screen:

```kotlin
val textHalfWidth = measuredText.size.width / 2
val textHalfHeight = measuredText.size.height / 2

drawText(
    textLayoutResult = measuredText,
    topLeft = Offset(
        x = detectedObjectBoundingBox.center.x - textHalfWidth,
        y = detectedObjectBoundingBox.center.y - textHalfHeight
    )
)
```

## Add it all together

Now we just need to call the ObjectTrackingOverlay from our `ObjetTracking` composable:

```kotlin
@Composable
fun ObjectTracking() {
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
                    detectedObjects = objects
                }
            )
        }
    }

    CameraPreview(cameraController)

    ObjectTrackingOverlay(detectedObjects = detectedObjects)
}
```

The only new line here is the line at the bottom, calling the `ObjectTrackingOverlay()`. We're passing in detected objects returned by the image analyzer. Please be careful about the order of composables. If you first called `ObjectTrackingOverlay` and then `CameraPreview`, you'd not see the rectangle tracking the object.

## Are we finished?

And that's it. We've used CameraX library together with MLKit, to track objects and draw a tracking rectangle on screen using Jetpack Compose. So we're finished, right? Actually it would probably be disappointing to finish now, wouldn't it? You may have noticed that the `imageSize` of the `onDetectorSuccess` callback is not used. Not only that, no doubt at least some readers of this tutorial have tried to run the code and noticed that the rectangle drawn on the screen is not properly positioned in respect to the tracked object and it's also too small. 

Aren't we going to fix it? Yes, we are. See you again in [Step 5](step_5.md) of this tutorial, where we'll scale and place the rectangle properly.
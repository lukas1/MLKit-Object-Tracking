# Step 3

Having completed *Step 1* and *Step 2* we're ready to prepare for object tracking. To track objects by camera, we'll turn to MLKit. 

With MLKit we have two main options for tracking objects. We can either use the base model provided by the MLKit, or we could turn to custom models. The advantage of using the base model is, that it is simpler and we don't need to look for any pretrained model. The disadvantage is, that the model is fixed. Advantage of custom model is, that we can use any pretrained model, which gives us flexibility to choose. It's easy to change the functionality of an app that uses pretrained models from tracking generic objects to tracking more specific objects, such as landmarks, types of animals, plants, etc.

Therefore for this demo we'll turn to custom models. There are two options here as well. We could either use a model bundled into the app binary, or a remote model, that is hosted somewhere on Firebase. In this demo we'll use a model bundled into our binary. If you want to learn more about using remote models, look into official [MLKit documentation](https://developers.google.com/ml-kit/vision/object-detection/custom-models/android).

## Finding a model

Now that we've decided to use a custom model, we need to find a suitable model. The important thing to note here is, that we can't use just about any model. We need a model compatible with the MLKit. We can find a list of compatible, publicly freely available models here: [https://tfhub.dev/ml-kit/collections/image-classification/1](https://tfhub.dev/ml-kit/collections/image-classification/1)

In this demo we're going to use the [mnasnet_1.3_224 model](https://tfhub.dev/tensorflow/lite-model/mnasnet_1.3_224/1/metadata/1), which provides labels for the tracked objects. Please note, that this model is far from perfect and it will label objects incorrectly quite often. That said, it has no issues with tracking. Download the model and add it to the assets folder of the project.

## Dependencies

We'll add MLKit library:

```kotlin
    // MLKit dependencies
    implementation("com.google.mlkit:object-detection-custom:17.0.0")
```

## Prepare ObjectDetector

Now we're ready to create an instance of `ObjectDetector`, to which we can feed image from camera and it will return list of detected objects along with labels and a `Rect` that denotes where in the provided image the object resides. Note that the model that we're using in this demo supports tracking and detecting only the most prominent object in the image, therefore the list of detected objects will always contain at most only 1 element. We can add a new file `CustomObjectDetector` to our project and add to it the following method:

```kotlin
private fun customObjectDetector(): ObjectDetector {
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
```

First we're parsing the tflite model and then we're providing it to build `ObjectDetector`. We're setting detector mode to `STREAM_MODE`. There are two detector modes. `SINGLE_IMAGE_MODE` and `STREAM_MODE`. The `SINGLE_IMAGE_MODE` is used when we just have a single image that we need to analyze. Using this mode is perfect, for analyzing captured image, but it is too slow for processing every single frame from camera to get live object tracking. Instead, `STREAM_MODE` relies on the fact, that camera frames tend to be very similar and it will not process every single frame as a new standalone image, instead it will reuse some of the precalculated information from the previous frames to perform the analysis.

We're also setting a classification confidence threshold. We can pick any number between 0 and 1. Theoretically this should mean less mistakes when classifying objects. In practice, with the particular model used in this demo the difference is almost negligible.

## Feed images to the `ObjectDetector`

Now that we have a way to construct the object detector, we need to be able to feed images into it. CameraX library has APIs ready to work with MLKit so that providing images to MLKit is easy. First, we need to construct an `ImageAnalysis.Analyzer` and then connect this `ImageAnalysis.Analyzer` to the `CameraController` that is controlling the camera. Let's construct an `ImageAnalysis.Analyzer`, then. Create a new file, called `ObjectTrackingImageAnalyzer` and create a class of the same name in the file, like this:

```kotlin
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
```

Our class implements `ImageAnalysis.Analyzer` interface. The interface requires us to implement the method `analyze(imageProxy: ImageProxy)`, which is called by the CameraX library. It is in this method, where we get the image that we can pass to the `ObjectDetector`, that we're passing in the constructor. To detect objects in the passed image, we call the `process` method on the `ObjectDetector`. We can then hook on to some of the callbacks for when the `ObjectDetector` is either finished with analyzing the image, or if an error occurred. In this case we ignore the error handling to keep things simple and instead only hook to on success listener and on complete listener. It is important to hook to the on complete listener and call `close()` on the imageProxy parameter from the `analyze` method. This call helps to free the memory after the image is finally analyzed and processed. Not calling this method could mean our app would throw `OutOfMemoryException` sooner or later.

We're passing the list of detected objects to a callback `onDetectorSuccess`. This way we can get the results out of the `ObjectTrackingImageAnalyzer` and use them in our composables. Note that not only are we returning the list of detected objects, we're also returning the size of the image. Don't worry about it now, it will be needed in later step, when we'll draw a rectangle, tracking the detected object on the screen.

Now we have made all the preparations to use MLKit in our app. See you again at [Step 4](step_4.md), where we'll talk about how to connect the image analyzer to the camera controller.
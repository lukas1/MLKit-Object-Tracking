# Step 2

In *Step 1* we have requested from user permissions to camera. Now it's time to use that permission and show the user a camera preview.

## Prepare our ObjectTracking composable

As our ultimate goal is to be able to track objects on the screen, we'll create a new composable, where user will see the camera preview and the tracked object will be marked by a rectangle. So let's create an empty composable where our code will reside.

```kotlin
@Composable
fun ObjectTracking() {

}
```

We'll call this composable from the MainScreen's composable in the successful camera permission request case:

```kotlin
if (cameraPermissionState.status.isGranted) {
    ObjectTracking()
}
```

## Dependencies
Now we add dependencies for the CameraX library:

```kotlin
    // CameraX dependencies
    val cameraXVersion = "1.2.3"
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
```

## Display the Camera Preview

With the dependencies resolved, we can show the camera preview. Unfortunately the CameraX library does not provide any Jetpack Compose composable. It only provides `PreviewView` for the legacy Android view system. Thankfully Jetpack Compose enables us to include Android Views into our composable, using the `AndroidView` composable. We'll create a new `CameraPreview` composable to host the camera code:

```kotlin
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
```

Now we can call this composable from the `ObjectTracking` composable.

```kotlin
@Composable
fun ObjectTracking() {
    // ... create the cameraController

    CameraPreview(cameraController)
}
```

As you can see, our composable has a dependency. We need to pass `CameraController`. What is it? It's a class provided by the CameraX library. `CameraController` is used to control camera. Actually CameraX library provides other means of controlling the camera, that enable finer control, but this is not going to be necessary for our use case. Therefore we'll stick to using `CameraController`.

How to construct a `CameraController`? Luckily, the CameraX library provides `LifecycleCameraController` that properly handles lifecycle changes for us. Add the following code to the `ObjectTracking` composable to create an instance of the `LifecycleCameraController`. 

```kotlin
val context = LocalContext.current
val lifecycleOwner = LocalLifecycleOwner.current

val cameraController = remember {
    LifecycleCameraController(context).apply {
        bindToLifecycle(lifecycleOwner)
    }
}
```

As you can see, not only we're creating the instance of LifecycleCameraController, we're also binding lifecycle owner to it. Lifecycle owner is usually an `Activity` or `Fragment`. In this case the code is not added to an `Activity` nor a `Fragment`, so we don't have direct access to the `context` or the `lifecycleOwner`. Luckily, through Jetpack Compose calls via `LocalContext.current` and `LocalLifecycleOwner.current` we can get to the instance of the closest `Activity` or `Fragment` that is responsible for housing the currently running composable.

Notice also, that we're wrapping the initialization of `LifecycleCameraController` into a `remember` block. This is used to avoid recreating the `CameraController` on every recomposition. That would be costly and entirely unnecessary.

Not that it would also be possible to create an instance of the `LifecycleCameraController` in the `MainActivity` and pass it down to the composable, but this way our activity remains clean and simple. No need for the activity to be concerned about what it children composables are up to. Also, this allows us to avoid creating the instance of `CameraController` if the user did not grant permissions to camera.

And that's it. With this code, user is able to see on the screen a functioning camera preview. See you again in Step 3, where we'll start preparations for object tracking.
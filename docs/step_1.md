# Step 1

The goal of this step will be to present user with a screen that contains a button to request permissions for camera using Jetpack Compose and the Accompanist library.

## Prepare our main screen composable

To have the MainActivity tidy, we'll focus our code into a separate composable to represent the main screen. Thanks to this approach this composable becomes easily reusable.

Create a new file `MainScreen` and add for now an empty compose function like this

```kotlin
@Composable
fun MainScreen() {
    
}
```

Don't forget to add call this composable from the `MainActivity`:

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MLKitJetpackComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
```

## Dependencies

Open your app's `build.gradle.kts` file and add following dependency:

```kotlin
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
```

As you can see, we're adding the Permissions package of the `Accompanist` library. This will make it easy to request permissions for camera from the user.

## Permissions

As we want to use camera, we need to ask the user to grant us the permission to do so. As a first step we need to list the permission in the `AndroidManifest.xml` file. We also want to specify that the app needs camera hardware to work. Add the below code to your `AndroidManifest.xml` file.

```xml
    <uses-feature android:name="android.hardware.camera.any" />
    <uses-permission android:name="android.permission.CAMERA" />
```

Now we need to ask for user's permissions. We'll do this in the `MainScreen` composable that we've created at the beginning. The setup for this is very simple:

```kotlin
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        // TODO: Show camera view
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Please grant permissions to use the camera")
            Button(onClick = cameraPermissionState::launchPermissionRequest) {
                Text(text = "Grant permission")
            }
        }
    }
```

Note that the `rememberPermissionState`, which comes from the Accompanist library requires us to annotate our composable with the `@OptIn(ExperimentalPermissionsApi::class)` annotation.

Now when the user opens the app for the first time, they'll see a screen asking them to grant permissions to camera. We can also launch the request automatically using the `LaunchEffect` composable, like this:

```kotlin
    LaunchedEffect(key1 = "permission_request", block = {
        cameraPermissionState.launchPermissionRequest()
    })
```

in the `else` block of the `MainScreen` composable. Now when the user runs the app, the permission request dialogue is opened automatically.

And with that, we're done with the Step 1! See you again at [Step 2](step_2.md).
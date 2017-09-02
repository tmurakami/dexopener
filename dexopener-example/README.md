# Example

This application contains the following examples as product flavors:

- `basic` flavor

An example using the `DexOpenerAndroidJUnitRunner`.
If the package name of your app's `BuildConfig` is equal to the app's `applicationId`, you can just specify the `DexOpenerAndroidJUnitRunner` as the default test instrumentation runner in your `build.gradle`

- `usingApplicationIdSuffix` flavor

An example using the DexOpener library with `applicationIdSuffix`.
If you are using `applicationIdSuffix` in your `build.gradle`, you need to put your `AndroidJUnitRunner` subclass into the instrumented tests directory and call `DexOpener.Builder#buldConfig(Class)` in order to specify the app's `BuildConfig` class.

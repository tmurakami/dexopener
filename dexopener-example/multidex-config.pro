# Keep the app's BuildConfig in order for DexOpener to load it.
#
# The `APPLICATION_ID` is checked to see if it is equal to the app's `applicationId`, so it must be
# preserved.
-keep class com.example.dexopener.BuildConfig {
    public static java.lang.String APPLICATION_ID;
}

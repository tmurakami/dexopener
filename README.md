# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)
[![Javadoc](https://img.shields.io/badge/Javadoc-0.11.0-brightgreen.svg)](https://jitpack.io/com/github/tmurakami/dexopener/0.11.0/javadoc/)<br>
![Android](https://img.shields.io/badge/Android-4.1%2B-blue.svg)

A library that provides the ability to mock final classes and methods on Android.

## Example

See the [example application](dexopener-example).

## Installation

First, add the [JitPack](https://jitpack.io/) repository to your build.gradle.

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

And then, add this library as `androidTestCompile` dependency.

```groovy
dependencies {
    androidTestCompile 'com.github.tmurakami:dexopener:x.y.z'
}
```

Finally, set `DexOpenerAndroidJUnitRunner` as the default test instrumentation runner.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 to 25
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpenerAndroidJUnitRunner'
    }
}
```

## Extending

To replace your `Application` instance while testing, all you need to do is extend `DexOpenerAndroidJUnitRunner` class instead of `AndroidJUnitRunner` and override the `newApplication(ClassLoader, String, Context)` method, as shown here:

```java
public class YourAndroidJUnitRunner extends DexOpenerAndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Do not call `Class#getName()` here to get the Application class name because class
        // inconsistency error occurs when classes are loaded before DexOpener manipulates the
        // DEX bytecode.
        return super.newApplication(cl, "your.app.TestApplication", context);
    }
}
```

If it is not possible to change the base class, you should call `DexOpener#install(Instrumentation)` before calling `super.newApplication(ClassLoader, String, Context)`.

```java
public class YourAndroidJUnitRunner extends OtherAndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.install(this);
        return super.newApplication(cl, "your.app.TestApplication", context);
    }
}
```

By default, DexOpener try to load `applicationId + ".BuildConfig"` in order to find the classes to be opened.
But if the package name of the BuildConfig is not equal to your app's `applicationId` (e.g., you are using `applicationIdSuffix` in your build.gradle), loading it will fail.
In that case, you should set your app's BuildConfig by using `DexOpener.Builder` like the following code:

```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.builder(context)
                .buildConfig(your.apps.BuildConfig.class) // Set your app's BuildConfig
                .build()
                .installTo(cl);
        return super.newApplication(cl, "your.app.TestApplication", context);
    }
}
```

## Limitations

- Mockable final classes and methods are restricted under the package of the app's BuildConfig.
- `minSdkVersion` cannot be set to `26` because [dexlib2](https://github.com/JesusFreke/smali) does not currently support version `038` of the DEX format.

## Notice

This library contains the classes of the following libraries:

- [ClassInjector](https://github.com/tmurakami/classinjector)
- [dexlib2 (part of smali/baksmali)](https://github.com/JesusFreke/smali)
- [Guava](https://github.com/google/guava)

These classes have been minified with [ProGuard](https://www.guardsquare.com/en/proguard) and repackaged with [Jar Jar Links](https://code.google.com/archive/p/jarjar/).

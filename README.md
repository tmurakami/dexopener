# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)
[![Javadoc](https://img.shields.io/badge/Javadoc-0.12.0-brightgreen.svg)](https://jitpack.io/com/github/tmurakami/dexopener/0.12.0/javadoc/)<br>
![Android](https://img.shields.io/badge/Android-4.1%2B-blue.svg)

A library that provides the ability to mock final classes and methods on Android.

## Example

See the [example application](dexopener-example).

## Usage

We provide the following classes to install DexOpener into your test application.

### `DexOpenerAndroidJUnitRunner`

If you do **NOT** specify `applicationIdSuffix` in your build.gradle, you can use this class as the default test instrumentation runner.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 to 25
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpenerAndroidJUnitRunner'
    }
}
```

For projects using `applicationIdSuffix`, use [`DexOpener.Builder`](#dexopenerbuilder) instead.

### `DexOpener`

If you already have your own AndroidJUnitRunner subclass, you can also use `DexOpener#install(Instrumentation)` instead of `DexOpenerAndroidJUnitRunner`.

```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.install(this); // Call this before super.newApplication()
        return super.newApplication(cl, className, context);
    }
}
```

### `DexOpener.Builder`

If you are specifying `applicationIdSuffix` in your build.gradle, you need to use this class.

By default, DexOpener tries loading `Context#getPackageName() + ".BuildConfig"` in order to find the classes to be opened.
Therefore, for projects using `applicationIdSuffix`, loading it will fail because the package name of the BuildConfig is not equal to the value of `Context#getPackageName()`.

To prevent this, put an AndroidJUnitRunner subclass like the following code into the project's instrumented tests directory.

```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.builder(context)
                .buildConfig(your.app.BuildConfig.class) // Set the BuildConfig class
                .build()
                .installTo(cl);
        return super.newApplication(cl, className, context);
    }
}
```

And then, specify it as the default test instrumentation runner.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 to 25
        testInstrumentationRunner 'your.app.YourAndroidJUnitRunner'
    }
}
```

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

[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)

> **Note:** If a NoClassDefFoundError for your app's BuildConfig is thrown by using Multidex, you must specify the BuildConfig in the primary DEX file.
> See https://developer.android.com/studio/build/multidex.html?hl=en#keep

## Limitations

- Mockable final classes and methods are restricted under the package of the app's BuildConfig.
- `minSdkVersion` cannot be set to `26` because [dexlib2](https://github.com/JesusFreke/smali) does not currently support version `038` of the DEX format.
- Do **NOT** load any class under your app package until the Application instance is created. If you have your own AndroidJUnitRunner subclass, loading your classes before calling `super.newApplication(ClassLoader, String, Context)` may cause class inconsistency error.

## Notice

This library contains the classes of the following libraries:

- [ClassInjector](https://github.com/tmurakami/classinjector)
- [dexlib2 (part of smali/baksmali)](https://github.com/JesusFreke/smali)
- [Guava](https://github.com/google/guava)

They have been minified with [ProGuard](https://www.guardsquare.com/en/proguard) and repackaged with [Jar Jar Links](https://code.google.com/archive/p/jarjar/).

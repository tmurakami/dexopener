# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)
[![Javadoc](https://img.shields.io/badge/Javadoc-0.12.1-brightgreen.svg)](https://jitpack.io/com/github/tmurakami/dexopener/0.12.1/javadoc/)<br>
![Android](https://img.shields.io/badge/Android-4.1%2B-blue.svg)

A library that provides the ability to mock
[your final classes/methods](#limitations_final_you_can_mock) on
Android.

## Example

See the [example application](dexopener-example).

## Usage

There are three ways to use this library.

### DexOpenerAndroidJUnitRunner

If you are **NOT** specifying `applicationIdSuffix` in your
build.gradle, you can use this class as the default test instrumentation
runner.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 to 25
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpenerAndroidJUnitRunner'
    }
}
```

To instantiate your custom `android.app.Application` object other than
default application, all you need to do is create a new test runner that
extends `DexOpenerAndroidJUnitRunner` instead of `AndroidJUnitRunner`.

```java
public class YourAndroidJUnitRunner extends DexOpenerAndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        /*
         * Don't call Class#getName() to get the Application class name, or a class
         * inconsistency error may occur. Instead, specify a string literal for its name.
         */
        return super.newApplication(cl, "your.app.YourTestApplication", context);
    }
}
```

Make sure to update your build.gradle with the new test runner.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 to 25
        testInstrumentationRunner 'your.app.YourAndroidJUnitRunner'
    }
}
```

For projects using `applicationIdSuffix`, use
[`DexOpener.Builder`](#dexopenerbuilder) instead.

### DexOpener

If it is not possible to change the base class of your test runner to
`DexOpenerAndroidJUnitRunner`, you can use
`DexOpener#install(Instrumentation)`.

```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Call this first.
        DexOpener.install(this);
        // Don't call Class#getName() here.
        return super.newApplication(cl, "your.app.YourTestApplication", context);
    }
}
```

### DexOpener.Builder

If you are specifying `applicationIdSuffix` in your build.gradle, you
need to use this class.

By default, DexOpener tries loading
`Context#getPackageName() + ".BuildConfig"` in order to find the classes
to be opened. Therefore, for projects using `applicationIdSuffix`,
loading it will fail because the package name of the BuildConfig is not
equal to the value of `Context#getPackageName()`.

To prevent this, put an AndroidJUnitRunner subclass like the following
code into the project's instrumented tests directory.

```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.builder(context)
                .buildConfig(your.app.BuildConfig.class)
                .build()
                .installTo(cl);
        // Don't call Class#getName() here.
        return super.newApplication(cl, "your.app.YourTestApplication", context);
    }
}
```

## Limitations

- <a name="limitations_final_you_can_mock"></a>The final classes/methods
you can mock are only those under the package of your app's BuildConfig.
Therefore, you cannot mock final classes/methods you don't own, such as
Android system classes and third-party libraries.
- `minSdkVersion` cannot be set to more than `26` because
[dexlib2](https://github.com/JesusFreke/smali) does not currently
support version `038` of the DEX format.

## Installation

First, add the [JitPack](https://jitpack.io/) repository to your
build.gradle.

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

> **Note:** If you are using
[Multidex](https://developer.android.com/studio/build/multidex.html?hl=en),
you need to specify your app's BuildConfig
[in the primary DEX file](https://developer.android.com/studio/build/multidex.html?hl=en#keep),
otherwise, you will get the NoClassDefFoundError.

## Notice

This library contains the classes of the following libraries:

- [ClassInjector](https://github.com/tmurakami/classinjector)
- [dexlib2 (part of smali/baksmali)](https://github.com/JesusFreke/smali)
- [Guava (on which dexlib2 relies)](https://github.com/google/guava)

They have been minified with
[ProGuard](https://www.guardsquare.com/en/proguard) and repackaged with
[Jar Jar Links](https://code.google.com/archive/p/jarjar/).

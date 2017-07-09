# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)
[![Javadoc](https://img.shields.io/badge/Javadoc-0.10.1-brightgreen.svg)](https://jitpack.io/com/github/tmurakami/dexopener/0.10.1/javadoc/)<br>
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

To replace the `Application` instance while testing, simply extend from `DexOpenerAndroidJUnitRunner`.
```java
public class YourAndroidJUnitRunner extends DexOpenerAndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, "your.app.TestApplication", context);
    }
}
```

If it is not possible to change the base class, call `DexOpener#install(Instrumentation)` before calling `super.newApplication()`.
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

By default, mockable final classes and methods are restricted under the package obtained by `Context#getPackageName()`.
To change this restriction, use `DexOpener.Builder#classNameFilter(ClassNameFilter)` or `DexOpener.Builder#openIf(ClassNameFilter)` which makes it easier to read with lambda expressions.
```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.builder(context)
                .openIf(name -> name.startsWith("package.you.want.to.mock."))
                .build()
                .installTo(cl);
        return super.newApplication(cl, "your.app.TestApplication", context);
    }
}
```

Note that testing might be slow if there are too many classes to be opened.
Normally it would be sufficient to specify your app's root package.

## Limitations

The final classes and methods in the following libraries cannot be mocked.

- [Android](https://developer.android.com/reference/packages.html)
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html)
- [Android Data Binding Library](https://developer.android.com/topic/libraries/data-binding/index.html)
- [Android Support Library](https://developer.android.com/topic/libraries/support-library/index.html)
- [Android Testing Support Library](https://developer.android.com/topic/libraries/testing-support-library/index.html)
- [Byte Buddy](http://bytebuddy.net/)
- [ClassInjector](https://github.com/tmurakami/classinjector)
- [Dexmaker](https://github.com/linkedin/dexmaker)
- [DexMockito](https://github.com/tmurakami/dexmockito)
- DexOpener
- [Mockito4k](https://github.com/tmurakami/mockito4k)
- [Hamcrest](https://github.com/hamcrest/JavaHamcrest)
- [JaCoCo](http://www.eclemma.org/jacoco/)
- [JUnit](http://junit.org/)
- [Kotlin](https://kotlinlang.org/)
- [Mockito](http://site.mockito.org/)
- [Objenesis](http://objenesis.org/)

Currently, `minSdkVersion` cannot be set to `26` because [dexlib2](https://github.com/JesusFreke/smali) does not support version `038` of the Dex format.

## Notice

This library contains the classes of the following libraries:

- [ClassInjector](https://github.com/tmurakami/classinjector)
- [dexlib2 (part of smali/baksmali)](https://github.com/JesusFreke/smali)
- [Guava](https://github.com/google/guava)

These classes have been minified with [ProGuard](https://www.guardsquare.com/en/proguard) and repackaged with [Jar Jar Links](https://code.google.com/archive/p/jarjar/).

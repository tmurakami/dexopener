# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)

A library that provides the ability to mock final classes and methods on Android.

## Installation

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    androidTestCompile 'com.github.tmurakami:dexopener:x.y.z'
}
```

And set DexOpener as the default test instrumentation runner.

```groovy
android {
    defaultConfig {
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpener'
    }
}
```

## Limitation

The following final classes and methods cannot be mocked.

- [Android APIs](https://developer.android.com/reference/packages.html), including [Support Library](https://developer.android.com/topic/libraries/support-library/index.html) and [Data Binding Library](https://developer.android.com/topic/libraries/data-binding/index.html)
- [Android Testing Support Library](https://developer.android.com/topic/libraries/testing-support-library/index.html)
- [Byte Buddy](http://bytebuddy.net/)
- [Dexmaker](https://github.com/crittercism/dexmaker)
- [DexMockito](https://github.com/tmurakami/dexmockito)
- DexOpener
- [JUnit](http://junit.org/)
- [Kotlin](https://kotlinlang.org/)
- [Mockito](http://site.mockito.org/)
- [Objenesis](http://objenesis.org/)

## Notice

This library includes [ASMDEX](http://asm.ow2.org/asmdex-index.html) that has been repackaged using [Jar Jar Links](https://code.google.com/archive/p/jarjar/).

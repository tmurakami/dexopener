# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)

A library that is able to mock final classes and methods on Dalvik/ART.

## Installation

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    androidTestCompile 'com.github.tmurakami:dexopener:x.y.z'
}

android {
    defaultConfig {
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpener'
    }
}
```

## Limitation

This library is **NOT** able to create *final* mocks of the following classes:

- Android Classes
- [Android Data Binding Library](https://developer.android.com/topic/libraries/data-binding/index.html)
- [Android Support Library](https://developer.android.com/topic/libraries/support-library/index.html)
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

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

## Notice

This library includes [ASMDEX](http://asm.ow2.org/asmdex-index.html) that has been repackaged using [Jar Jar Links](https://code.google.com/archive/p/jarjar/).

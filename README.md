# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)  
![Android](https://img.shields.io/badge/Android-4.1%2B-blue.svg)

A library that provides the ability to mock final classes and methods on Android.

## Example

The test target which is a final class:
```java
final class MyService {
    final Object doIt() {
        throw new UnsupportedOperationException();
    }
}
```

The test which runs on Android devices with mockito-android:
```java
public class MyServiceTest {
    @Test
    public void doIt() throws Exception {
        MyService service = mock(MyService.class);
        willReturn("test").given(service).doIt();
        assertEquals("test", service.doIt());
    }
}
```

There are a few examples in the [androidTest](https://github.com/tmurakami/dexopener/tree/master/src/androidTest/java/test/com/github/tmurakami/dexopener) directory.

## Limitation

The final classes and methods in the following libraries cannot be mocked.

- [Android APIs](https://developer.android.com/reference/packages.html), including [Support Library](https://developer.android.com/topic/libraries/support-library/index.html) and [Data Binding Library](https://developer.android.com/topic/libraries/data-binding/index.html)
- [Android Testing Support Library](https://developer.android.com/topic/libraries/testing-support-library/index.html)
- [Byte Buddy](http://bytebuddy.net/)
- [ClassInjector](https://github.com/tmurakami/classinjector)
- [Dexmaker](https://github.com/linkedin/dexmaker)
- [DexMockito](https://github.com/tmurakami/dexmockito)
- DexOpener
- [Hamcrest](https://github.com/hamcrest/JavaHamcrest)
- [JUnit](http://junit.org/)
- [Kotlin](https://kotlinlang.org/)
- [Mockito](http://site.mockito.org/)
- [Objenesis](http://objenesis.org/)

## Installation

First, add the [JitPack](https://jitpack.io/) repository to your build.gradle.
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

And then, add this library as 'androidTestCompile' dependency.
```
dependencies {
    androidTestCompile 'com.github.tmurakami:dexopener:x.y.z'
}
```

Finally, set DexOpenerRunner as the default test instrumentation runner.
```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 or higher
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpenerRunner'
    }
}
```

## Notice

This library includes [ASMDEX](http://asm.ow2.org/asmdex-index.html) (Revision 1707) that has been repackaged using [Jar Jar Links](https://code.google.com/archive/p/jarjar/).

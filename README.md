# DexOpener

[![CircleCI](https://circleci.com/gh/tmurakami/dexopener.svg?style=shield)](https://circleci.com/gh/tmurakami/dexopener)
[![Release](https://jitpack.io/v/tmurakami/dexopener.svg)](https://jitpack.io/#tmurakami/dexopener)
[![Javadoc](https://img.shields.io/badge/Javadoc-1.0.0-brightgreen.svg)](https://jitpack.io/com/github/tmurakami/dexopener/1.0.0/javadoc/)<br>
![Android](https://img.shields.io/badge/Android-4.1%2B-blue.svg)

A library that provides the ability to mock
[your final classes/methods](#limitations_final_you_can_mock) on
Android.

## Example

See the [example application](dexopener-example).

## Usage

There are two ways to use this library.

> **Note:** Starting at version 0.13.0, DexOpener automatically detects
the BuildConfig class of the target application. Therefore, you no
longer need to use `DexOpener.Builder`. `DexOpener.Builder` will be
deleted in the next major version.

### DexOpenerAndroidJUnitRunner

If you do not have your own test instrumentation runner, all you need to
do is specify `DexOpenerAndroidJUnitRunner` as the default test
instrumentation runner.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 or higher
        testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpenerAndroidJUnitRunner'
    }
}
```

You can extend this class to create your custom `AndroidJUnitRunner`.

```java
public class YourAndroidJUnitRunner extends DexOpenerAndroidJUnitRunner { ... }
```

If you want to replace the application instance for testing, extend this
class and implement `newApplication()` method as shown in
[Tips](#replacing-the-application-instance-for-testing).

### DexOpener

If you already have your own test instrumentation runner, you can use
`DexOpener` instead of `DexOpenerAndroidJUnitRunner`.

```java
public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.install(this);
        return super.newApplication(cl, className, context);
    }
}
```

> **Note:** If you are using a class literal to replace the
Application instance, you will need to use a string literal instead.
See [Tips](#replacing-the-application-instance-for-testing).

And make sure your test instrumentation runner is specified in your
build.gradle.

```groovy
android {
    defaultConfig {
        minSdkVersion 16 // 16 or higher
        testInstrumentationRunner 'your.app.YourAndroidJUnitRunner'
    }
}
```

## Tips

### Replacing the Application instance for testing

To instantiate your custom `android.app.Application` object other than
default application, pass a string literal of that class name as the
second argument to `super.newApplication()` in your test instrumentation
runner.

```java
@Override
public Application newApplication(ClassLoader cl, String className, Context context)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {

    ...

    return super.newApplication(cl, "your.app.YourTestApplication", context);
}
```

**Do not call `Class#getName()` to get the Application class name**. The
following code may cause a class inconsistency error.

```java
// This code may cause a class inconsistency error.
return super.newApplication(cl, YourTestApplication.class.getName(), context);
````

## Limitations

- <a name="limitations_final_you_can_mock"></a>The final classes you can
mock are only those under the package of your app's BuildConfig. For
example, if the FQCN of your BuildConfig is `foo.bar.BuildConfig`,
you can mock only the final classes belonging to `foo.bar.**`.
Therefore, you cannot mock final classes/methods of both Android system
classes and third-party libraries.

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
    androidTestCompile 'com.github.tmurakami:dexopener:1.0.0'
}
```

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
[Jar Jar Links](https://code.google.com/archive/p/jarjar/). In addition,
dexlib2 has been backported to Java 7 with
[Retrolambda](https://github.com/orfjackal/retrolambda).

## License

```
Copyright 2016 Tsuyoshi Murakami

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

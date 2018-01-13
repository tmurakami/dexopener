package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.test.runner.AndroidJUnitRunner;

/**
 * An {@link AndroidJUnitRunner} that provides the ability to mock your final classes/methods. To
 * use this, add the following into your build.gradle:
 * <pre><code>
 * android {
 *   defaultConfig {
 *     minSdkVersion 16 // 16 to 25
 *     testInstrumentationRunner 'com.github.tmurakami.dexopener.DexOpenerAndroidJUnitRunner'
 *   }
 * }
 *
 * repositories {
 *   maven { url 'https://jitpack.io' }
 * }
 *
 * dependencies {
 *   androidTestCompile 'com.github.tmurakami:dexopener:x.y.z'
 * }
 * </code></pre>
 * <a href="https://jitpack.io/#tmurakami/dexopener"><img alt="release" src="https://jitpack.io/v/tmurakami/dexopener.svg"></a>
 * <p>
 * If you have your own {@link AndroidJUnitRunner}, you can use {@link DexOpener} instead.
 *
 * @see AndroidJUnitRunner
 * @see DexOpener
 */
public class DexOpenerAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    @CallSuper
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.install(this);
        return super.newApplication(cl, className, context);
    }
}

/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

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

// You can mock only the final classes in this package and its subpackages.
package com.example.dexopener.replaceapp;

import android.app.Application;
import android.content.Context;

import com.github.tmurakami.dexopener.DexOpener;

import androidx.test.runner.AndroidJUnitRunner;

public class MyAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        DexOpener.install(this); // Call me first!
        // TODO https://github.com/mockito/mockito/issues/1472
        System.setProperty("org.mockito.android.target", context.getCacheDir().getAbsolutePath());
        // To instantiate your custom `android.app.Application` object other than default
        // application, pass a string literal of that class name as the second argument to
        // `super.newApplication()`.
        // Do not call `Class#getName()` here. Otherwise, a class inconsistency error may occur.
        return super.newApplication(cl, "com.example.dexopener.replaceapp.TestApp", context);
    }
}
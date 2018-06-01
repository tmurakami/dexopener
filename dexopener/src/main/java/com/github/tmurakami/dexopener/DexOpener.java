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

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import static com.github.tmurakami.dexopener.Constants.MY_PACKAGE;

/**
 * This is a utility that provides the ability to mock your final classes. To use this, first add an
 * AndroidJUnitRunner subclass into your app's <strong>androidTest</strong> directory.
 *
 * <pre><code>
 * // Specify your root package as `package` statement.
 * // The final classes you can mock are only in the package and its subpackages.
 * package your.root.pkg;
 *
 * public class YourAndroidJUnitRunner extends AndroidJUnitRunner {
 *     &#64;Override
 *     public Application newApplication(ClassLoader cl, String className, Context context)
 *             throws ClassNotFoundException, IllegalAccessException, InstantiationException {
 *         DexOpener.install(this); // Call me first!
 *         return super.newApplication(cl, className, context);
 *     }
 * }
 * </code></pre>
 * <p>
 * Then specify your AndroidJUnitRunner as the default test instrumentation runner in your app's
 * build.gradle.
 *
 * <pre><code>
 * android {
 *     defaultConfig {
 *         minSdkVersion 16 // 16 or higher
 *         testInstrumentationRunner 'your.root.pkg.YourAndroidJUnitRunner'
 *     }
 * }
 * </code></pre>
 */
public final class DexOpener {

    private static final String[] REFUSED_PACKAGES = {
            MY_PACKAGE + '.',
            // Android
            "android.",
            "androidx.",
            "com.android.",
            "com.google.android.",
            "com.sun.",
            "dalvik.",
            "java.",
            "javax.",
            "libcore.",
            "org.apache.commons.logging.",
            "org.apache.harmony.",
            "org.apache.http.",
            "org.ccil.cowan.tagsoup.",
            "org.json.",
            "org.kxml2.io.",
            "org.w3c.dom.",
            "org.xml.sax.",
            "org.xmlpull.v1.",
            "sun.",
            // JUnit 4
            "junit.",
            "org.hamcrest.",
            "org.junit.",
    };

    private DexOpener() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Provides the ability to mock your final classes.
     *
     * @param instrumentation the {@link Instrumentation} instance of your AndroidJUnitRunner
     *                        subclass
     * @throws IllegalArgumentException      if there is no '.' separator in the package of the
     *                                       given {@link Instrumentation} instance
     * @throws IllegalStateException         if the given {@link Instrumentation} instance has not
     *                                       yet been initialized, or if an
     *                                       {@link android.app.Application} instance has already
     *                                       been created
     * @throws UnsupportedOperationException if an {@link Instrumentation} instance belonging to
     *                                       a special package such as 'android' or 'androidx' is
     *                                       specified
     * @apiNote This method must be called first on the
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * newApplication(ClassLoader, String, Context)} method overridden in your AndroidJUnitRunner
     * subclass.
     */
    @SuppressWarnings("JavaDoc")
    public static void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            throw new IllegalStateException(
                    "The Instrumentation instance has not yet been initialized");
        }
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException("An Application instance has already been created");
        }
        String rootPackage = null;
        String instrumentationName = instrumentation.getClass().getName();
        int lastDotPos = instrumentationName.lastIndexOf('.');
        if (lastDotPos != -1) {
            String pkg = instrumentationName.substring(0, lastDotPos);
            if (pkg.indexOf('.') != -1) {
                rootPackage = pkg;
            }
        }
        if (rootPackage == null) {
            throw new IllegalArgumentException(
                    "The package of the given Instrumentation instance must have at least one " +
                    "'.' separator: " + instrumentationName);
        }
        for (String pkg : REFUSED_PACKAGES) {
            if (rootPackage.startsWith(pkg)) {
                throw new UnsupportedOperationException(
                        "Manipulating an Instrumentation instance belonging to the '" +
                        rootPackage + "' package is not supported");
            }
        }
        Logger logger = Loggers.get();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("The package to be opened: " + rootPackage + ".**");
        }
        install(context, new AndroidClassSourceFactory(
                new ClassNameFilter(rootPackage + '.').excludeClasses(instrumentationName)));
    }

    @VisibleForTesting
    static void install(Context context, AndroidClassSourceFactory androidClassSourceFactory) {
        ApplicationInfo ai = context.getApplicationInfo();
        File parentDir;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            parentDir = new File(ai.dataDir, "code_cache");
        } else {
            parentDir = context.getCodeCacheDir();
        }
        File cacheDir = new File(parentDir, "dexopener");
        if (cacheDir.isDirectory() || cacheDir.mkdirs()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        ClassSource classSource = androidClassSourceFactory.newClassSource(ai.sourceDir, cacheDir);
        ClassInjector.from(classSource).into(context.getClassLoader());
    }

}

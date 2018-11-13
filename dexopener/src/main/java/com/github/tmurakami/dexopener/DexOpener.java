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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import androidx.annotation.NonNull;

import static com.github.tmurakami.dexopener.Constants.MY_PACKAGE_PREFIX;

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
            MY_PACKAGE_PREFIX,
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

    private static final Executor EXECUTOR;

    static {
        final AtomicInteger count = new AtomicInteger();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int nThreads = Math.max(1, Math.min(availableProcessors, 4)); // 1 to 4
        EXECUTOR = Executors.newFixedThreadPool(
                nThreads, r -> new Thread(r, "DexOpener #" + count.incrementAndGet()));
    }

    private DexOpener() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Provides the ability to mock your final classes.
     *
     * @param instrumentation the {@link Instrumentation} instance of your AndroidJUnitRunner
     *                        subclass
     * @throws IllegalStateException         if this method is called twice or is called in an
     *                                       inappropriate location
     * @throws UnsupportedOperationException if the given {@link Instrumentation} instance belongs
     *                                       to a special package such as 'android'
     * @apiNote This method must be called first on the
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * newApplication(ClassLoader, String, Context)} method overridden in your AndroidJUnitRunner
     * subclass.
     */
    public static void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            String instrumentationName = instrumentation.getClass().getSimpleName();
            throw new IllegalStateException(
                    "The " + instrumentationName + " instance has not yet been initialized");
        }
        Context app = context.getApplicationContext();
        if (app != null) {
            throw new IllegalStateException(
                    "The " + app.getClass().getSimpleName() + " instance has already been created");
        }
        ClassLoader loader = context.getClassLoader();
        for (ClassLoader l = loader; l != null; l = l.getParent()) {
            if (l instanceof ClassInjector) {
                throw new IllegalStateException("Already installed");
            }
        }
        DexNameFilter dexNameFilter = createDexNameFilter(instrumentation.getClass());
        ClassPath classPath = new ClassPath(context, dexNameFilter, new DexFileLoader(), EXECUTOR);
        ClassLoaderHelper.setParent(loader, new ClassInjector(loader, classPath));
    }

    private static DexNameFilter createDexNameFilter(Class<?> rootClass) {
        String className = rootClass.getName();
        int lastDotPos = className.lastIndexOf('.');
        String packageName = lastDotPos == -1 ? null : className.substring(0, lastDotPos);
        if (isSupportedPackage(packageName)) {
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("The final classes under " + packageName + " will be opened");
            }
            return new DexNameFilter(packageName, rootClass);
        }
        throw new UnsupportedOperationException(
                "Install to an Instrumentation instance the package of which is " + packageName);
    }

    private static boolean isSupportedPackage(String packageName) {
        if (packageName == null || packageName.indexOf('.') == -1) {
            return false;
        }
        for (String pkg : REFUSED_PACKAGES) {
            if (packageName.startsWith(pkg)) {
                return false;
            }
        }
        return true;
    }

}

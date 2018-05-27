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

/**
 * This is an object that provides the ability to mock your final classes.
 * <p>
 * Note that the final classes you can mock are only those under the package indicated by
 * android.defaultConfig.applicationId in your build.gradle. For example, if it is foo.bar, you can
 * mock only the final classes belonging in foo.bar.**, such as foo.bar.Baz and foo.bar.qux.Quux.
 * Therefore, you cannot mock the final classes of both Android system classes and third-party
 * libraries, and cannot mock the final classes not belonging in that package, even if they are
 * yours.
 */
public final class DexOpener {

    private DexOpener() {
        throw new AssertionError("Do not instantiate");
    }

    /**
     * Provides the ability to mock your final classes.
     * <p>
     * Note that this method must be called before calling
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * super.newApplication(ClassLoader, String, Context)}.
     *
     * @param instrumentation the instrumentation
     */
    public static void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            throw new IllegalStateException(
                    "The Instrumentation instance has not yet been initialized");
        }
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException("An Application instance has already been created");
        }
        install(context, newAndroidClassSourceFactory(context));
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

    private static AndroidClassSourceFactory newAndroidClassSourceFactory(Context context) {
        Logger logger = Loggers.get();
        String applicationId = context.getPackageName();
        String packageToBeOpened = applicationId;
        while (true) {
            try {
                Class<?> c = Class.forName(packageToBeOpened + ".BuildConfig");
                if (!applicationId.equals(c.getField("APPLICATION_ID").get(null))) {
                    continue;
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Package to be opened: " + packageToBeOpened + ".**");
                }
                return new AndroidClassSourceFactory(new ClassNameFilter(packageToBeOpened + '.'));
            } catch (Exception ignored) {
            }
            int lastDotPos = packageToBeOpened.lastIndexOf('.');
            if (lastDotPos == -1) {
                throw new IllegalStateException(
                        "The BuildConfig class of the target application could not be found.");
            }
            packageToBeOpened = packageToBeOpened.substring(0, lastDotPos);
        }
    }

}

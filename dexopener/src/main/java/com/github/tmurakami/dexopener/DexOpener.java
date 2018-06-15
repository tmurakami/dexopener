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
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

/**
 * This is an object that provides the ability to mock your final classes/methods.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DexOpener {

    DexOpener() {
    }

    /**
     * Provides the ability to mock your final classes/methods.
     * <p>
     * Note that this method must be called before calling
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * super.newApplication(ClassLoader, String, Context)}.
     *
     * @param instrumentation the instrumentation
     */
    @SuppressWarnings("deprecation")
    public static void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            throw new IllegalArgumentException("'instrumentation' has not yet been initialized");
        }
        Class<?> buildConfigClass = findBuildConfigClass(context);
        builder(context).buildConfig(buildConfigClass).build().installTo(context.getClassLoader());
    }

    /**
     * Provides the ability to mock your final classes/methods. After calling this method, you can
     * mock classes loaded by the given class loader.
     * <p>
     * Note that this method must be called before calling
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * super.newApplication(ClassLoader, String, Context)}.
     *
     * @param target the class loader
     * @deprecated Starting at version 0.13.0, DexOpener automatically detects the BuildConfig class
     * of the target application. Therefore, you no longer need to use {@link Builder} to create a
     * {@link DexOpener} instance.
     */
    @SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
    @Deprecated
    public abstract void installTo(@NonNull ClassLoader target);

    /**
     * Instantiates a new {@link Builder} instance.
     *
     * @param context the context
     * @return the {@link Builder}
     * @deprecated Starting at version 0.13.0, DexOpener automatically detects the BuildConfig class
     * of the target application. Therefore, you no longer need to use {@link Builder} to create a
     * {@link DexOpener} instance.
     */
    @SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
    @Deprecated
    @NonNull
    @CheckResult
    public static Builder builder(@NonNull Context context) {
        return new Builder(context);
    }

    private static Class<?> findBuildConfigClass(Context context) {
        String packageName = context.getPackageName();
        while (true) {
            try {
                return Class.forName(packageName + ".BuildConfig");
            } catch (ClassNotFoundException e) {
                int lastDotPos = packageName.lastIndexOf('.');
                if (lastDotPos == -1) {
                    break;
                }
                packageName = packageName.substring(0, lastDotPos);
            }
        }
        throw new IllegalStateException(
                "The BuildConfig class of the target application could not be found.");
    }

    /**
     * The builder for {@link DexOpener}.
     *
     * @deprecated Starting at version 0.13.0, DexOpener automatically detects the BuildConfig class
     * of the target application. Therefore, you no longer need to use {@link Builder} to create a
     * {@link DexOpener} instance.
     */
    @SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
    @Deprecated
    public static final class Builder {

        private final Context context;
        private String packageToBeOpened;

        private Builder(Context context) {
            this.context = context;
        }

        /**
         * Sets the app's BuildConfig class. Only those final classes which is under the package of
         * the given class can be mocked. If not set, DexOpener will try to find the class which
         * name is {@link Context#getPackageName()} + ".BuildConfig".
         * <p>
         * If the package name of the BuildConfig is not equal to your app's applicationId
         * (e.g. you are using applicationIdSuffix in your build.gradle), DexOpener cannot find the
         * BuildConfig class. In such case, you should set your app's BuildConfig class using this
         * method.
         *
         * @param buildConfigClass the app's BuildConfig class
         * @return this builder
         * @deprecated Starting at version 0.13.0, DexOpener automatically detects the BuildConfig
         * class of the target application. Therefore, you no longer need to use {@link Builder} to
         * create a {@link DexOpener} instance.
         */
        @Deprecated
        @NonNull
        @CheckResult
        public Builder buildConfig(@NonNull Class<?> buildConfigClass) {
            String applicationId = null;
            if (buildConfigClass.getSimpleName().equals("BuildConfig")) {
                try {
                    applicationId = (String) buildConfigClass.getField("APPLICATION_ID").get(null);
                } catch (NoSuchFieldException ignored) {
                } catch (IllegalAccessException ignored) {
                }
            }
            String packageToBeOpened = null;
            if (context.getPackageName().equals(applicationId)) {
                packageToBeOpened = retrievePackageName(buildConfigClass);
            }
            if (packageToBeOpened == null || packageToBeOpened.isEmpty()) {
                throw new IllegalArgumentException(
                        "'buildConfigClass' must be the BuildConfig class of the target application");
            }
            this.packageToBeOpened = packageToBeOpened;
            return this;
        }

        /**
         * Instantiates a new {@link DexOpener} instance.
         *
         * @return the {@link DexOpener}
         * @deprecated Starting at version 0.13.0, DexOpener automatically detects the BuildConfig
         * class of the target application. Therefore, you no longer need to use {@link Builder} to
         * create a {@link DexOpener} instance.
         */
        @Deprecated
        @NonNull
        @CheckResult
        public DexOpener build() {
            String packageToBeOpened = this.packageToBeOpened;
            if (packageToBeOpened == null) {
                Class<?> buildConfigClass = loadBuildConfigClass(context);
                packageToBeOpened = this.packageToBeOpened = retrievePackageName(buildConfigClass);
            }
            ClassNameFilter filter = new ClassNameFilter(packageToBeOpened + '.');
            return new DexOpenerImpl(context, new AndroidClassSourceFactory(filter));
        }

        private static String retrievePackageName(Class<?> c) {
            String className = c.getName();
            return className.substring(0, className.lastIndexOf('.'));
        }

        private static Class<?> loadBuildConfigClass(Context context) {
            ClassLoader loader = context.getClassLoader();
            String name = context.getPackageName() + ".BuildConfig";
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "The BuildConfig of the target application could not be found.\n" +
                        "You need to put an AndroidJUnitRunner subclass like below " +
                        "in the instrumented tests directory and specify it as the " +
                        "default test instrumentation runner in the project's " +
                        "build.gradle.\n\n" +
                        "public class YourAndroidJUnitRunner extends AndroidJUnitRunner {\n" +
                        "    @Override\n" +
                        "    public Application newApplication(ClassLoader cl, String className, Context context)\n" +
                        "            throws InstantiationException, IllegalAccessException, ClassNotFoundException {\n" +
                        "        DexOpener.builder(context)\n" +
                        "                 .buildConfig(target.application.BuildConfig.class) // Set the BuildConfig class\n" +
                        "                 .build()\n" +
                        "                 .installTo(cl);\n" +
                        "        return super.newApplication(cl, className, context);\n" +
                        "    }\n" +
                        "}");
            }
        }

    }

}

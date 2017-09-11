package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;

/**
 * This is an object that provides the ability to mock final classes and methods.
 */
@SuppressWarnings("WeakerAccess")
public final class DexOpener {

    private final Context context;
    private final AndroidClassSourceFactory androidClassSourceFactory;
    private final ClassInjectorFactory classInjectorFactory;

    DexOpener(Context context,
              AndroidClassSourceFactory androidClassSourceFactory,
              ClassInjectorFactory classInjectorFactory) {
        this.context = context;
        this.androidClassSourceFactory = androidClassSourceFactory;
        this.classInjectorFactory = classInjectorFactory;
    }

    /**
     * Provides the ability to mock final classes and methods. This is equivalent to the following
     * code:
     * <pre>{@code
     * Context context = instrumentation.getTargetContext();
     * builder(context).build().installTo(context.getClassLoader());
     * }</pre>
     * <p>
     * Note that this method must be called before calling
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * super.newApplication(ClassLoader, String, Context)}.
     *
     * @param instrumentation the instrumentation
     * @see #builder(Context)
     * @see #installTo(ClassLoader)
     * @see Builder#build()
     */
    public static void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            throw new IllegalArgumentException("'instrumentation' has not yet been initialized");
        }
        builder(context).build().installTo(context.getClassLoader());
    }

    /**
     * Provides the ability to mock final classes and methods. After calling this method, you can
     * mock classes loaded by the given class loader.
     * <p>
     * Note that this method must be called before calling
     * {@link Instrumentation#newApplication(ClassLoader, String, Context)
     * super.newApplication(ClassLoader, String, Context)}.
     *
     * @param target the class loader
     */
    public void installTo(@NonNull ClassLoader target) {
        Context context = this.context;
        ApplicationInfo ai = context.getApplicationInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ai.minSdkVersion >= 26) {
            // dexlib2 does not currently support version `038` of the DEX format added in the
            // Android O.
            throw new UnsupportedOperationException("minSdkVersion must be lower than 26");
        }
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException(
                    "This method must be called before the Application instance is created");
        }
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        ClassSource classSource = androidClassSourceFactory.newClassSource(ai.sourceDir, cacheDir);
        classInjectorFactory.newClassInjector(classSource).into(target);
    }

    /**
     * Instantiates a new {@link Builder} instance.
     *
     * @param context the context
     * @return the {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull Context context) {
        return new Builder(context);
    }

    /**
     * The builder for {@link DexOpener}.
     */
    @SuppressWarnings("deprecation")
    public static final class Builder {

        private final Context context;
        private Class<?> buildConfigClass;

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
         */
        @NonNull
        public Builder buildConfig(@NonNull Class<?> buildConfigClass) {
            String applicationId = null;
            try {
                applicationId = (String) buildConfigClass.getField("APPLICATION_ID").get(null);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException ignored) {
            }
            if (!context.getPackageName().equals(applicationId)) {
                throw new IllegalArgumentException(
                        "'buildConfigClass' must be the BuildConfig class for the target application");
            }
            this.buildConfigClass = buildConfigClass;
            return this;
        }

        /**
         * Instantiates a new {@link DexOpener} instance.
         *
         * @return the {@link DexOpener}
         */
        @NonNull
        public DexOpener build() {
            if (buildConfigClass == null) {
                buildConfigClass = loadBuildConfigClass();
            }
            return new DexOpener(context, newAndroidClassSourceFactory(), new ClassInjectorFactory());
        }

        private Class<?> loadBuildConfigClass() {
            ClassLoader loader = context.getClassLoader();
            String name = context.getPackageName() + ".BuildConfig";
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "The BuildConfig for the target application could not be found.\n"
                                + "You need to put an AndroidJUnitRunner subclass like below "
                                + "in the instrumented tests directory and specify it as the "
                                + "default test instrumentation runner in the project's "
                                + "build.gradle.\n\n"
                                + "public class YourAndroidJUnitRunner extends AndroidJUnitRunner {\n"
                                + "    @Override\n"
                                + "    public Application newApplication(ClassLoader cl, String className, Context context)\n"
                                + "            throws InstantiationException, IllegalAccessException, ClassNotFoundException {\n"
                                + "        DexOpener.builder(context)\n"
                                + "                 .buildConfig(target.application.BuildConfig.class) // Set the BuildConfig class\n"
                                + "                 .build()\n"
                                + "                 .installTo(cl);\n"
                                + "        return super.newApplication(cl, className, context);\n"
                                + "    }\n"
                                + "}");
            }
        }

        private AndroidClassSourceFactory newAndroidClassSourceFactory() {
            Class<?> buildConfigClass = this.buildConfigClass;
            String className = buildConfigClass.getName();
            String simpleName = buildConfigClass.getSimpleName();
            String packagePrefix = className.substring(0, className.lastIndexOf(simpleName));
            return new AndroidClassSourceFactory(new ClassNameFilter(packagePrefix));
        }

    }

}

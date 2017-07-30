package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

/**
 * This is an object that provides the ability to mock final classes and methods.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DexOpener {

    DexOpener() {
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
     * @param classLoader the class loader
     */
    public abstract void installTo(@NonNull ClassLoader classLoader);

    /**
     * Instantiates a new {@link Builder} instance.
     *
     * @param context the context
     * @return the {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull Context context) {
        return new Builder(context,
                           new DexFileLoader(),
                           new DexClassSourceFactory(new DexClassFileFactory()));
    }

    /**
     * The builder for {@link DexOpener}.
     */
    @SuppressWarnings("deprecation")
    public static final class Builder {

        private final Context context;
        private final DexFileLoader dexFileLoader;
        private final DexClassSourceFactory dexClassSourceFactory;
        private ClassNameFilter classNameFilter;

        private Builder(Context context,
                        DexFileLoader dexFileLoader,
                        DexClassSourceFactory dexClassSourceFactory) {
            this.context = context;
            this.dexFileLoader = dexFileLoader;
            this.dexClassSourceFactory = dexClassSourceFactory;
        }

        /**
         * Sets a {@link ClassNameFilter}.
         *
         * @param filter the {@link ClassNameFilter}
         * @return this builder
         * @see #buildConfig(Class)
         * @deprecated Use {@link #buildConfig(Class)} if your app's root package is different from
         * the value obtained by calling {@link Context#getPackageName()}. This will be removed in
         * the future.
         */
        @Deprecated
        @NonNull
        public Builder openIf(@NonNull ClassNameFilter filter) {
            return classNameFilter(filter);
        }

        /**
         * Sets a {@link ClassNameFilter}.
         *
         * @param filter the {@link ClassNameFilter}
         * @return this builder
         * @see #buildConfig(Class)
         * @deprecated Use {@link #buildConfig(Class)} if your app's root package is different from
         * the value obtained by calling {@link Context#getPackageName()}. This will be removed in
         * the future.
         */
        @Deprecated
        @NonNull
        public Builder classNameFilter(@NonNull ClassNameFilter filter) {
            classNameFilter = filter;
            return this;
        }

        /**
         * Sets the app's BuildConfig class. Only those final classes which is under the package of
         * the given class can be mocked. If not set, DexOpener will try to find the class which
         * name is {@link Context#getPackageName()} + ".BuildConfig".
         * <p>
         * If the package name of the BuildConfig is not equal to your app's applicationId
         * (e.g., you are using applicationIdSuffix in your build.gradle), DexOpener cannot find the
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
                Field field = buildConfigClass.getField("APPLICATION_ID");
                applicationId = (String) field.get(null);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if (!context.getPackageName().equals(applicationId)) {
                throw new IllegalArgumentException(
                        "'buildConfigClass' must be your app's BuildConfig.class");
            }
            String className = buildConfigClass.getName();
            String simpleName = buildConfigClass.getSimpleName();
            final String packagePrefix = className.substring(0, className.lastIndexOf(simpleName));
            classNameFilter = new ClassNameFilter() {
                @Override
                public boolean accept(@NonNull String className) {
                    return className.startsWith(packagePrefix);
                }
            };
            return this;
        }

        /**
         * Instantiates a new {@link DexOpener} instance.
         *
         * @return the {@link DexOpener}
         */
        @NonNull
        public DexOpener build() {
            return new DexOpenerImpl(context,
                                     new ClassNameFilterWrapper(getClassNameFilter()),
                                     dexFileLoader,
                                     dexClassSourceFactory);
        }

        private ClassNameFilter getClassNameFilter() {
            if (classNameFilter == null) {
                Context context = this.context;
                String buildConfigName = context.getPackageName() + ".BuildConfig";
                ClassLoader loader = context.getClassLoader();
                try {
                    buildConfig(loader.loadClass(buildConfigName));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("BuildConfig.class must be set");
                }
            }
            return classNameFilter;
        }

    }

}

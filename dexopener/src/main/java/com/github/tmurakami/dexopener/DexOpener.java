package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is an object that provides the ability to mock final classes and methods.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DexOpener {

    DexOpener() {
    }

    /**
     * Provides the ability to mock final classes and methods.
     * <p>
     * This is equivalent to the following code:
     * <pre>{@code
     * Context context = instrumentation.getTargetContext();
     * builder(context).build().install(context.getClassLoader());
     * }</pre>
     *
     * @param instrumentation the instrumentation
     * @see #install(ClassLoader)
     */
    public static void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            throw new IllegalArgumentException("'instrumentation' has not been initialized yet");
        }
        builder(context).build().install(context.getClassLoader());
    }

    /**
     * Provides the ability to mock final classes and methods.
     * After calling this method, you can mock classes loaded by the given class loader.
     * <p>
     * Note that final classes loaded before calling this cannot be mocked.
     *
     * @param classLoader the class loader
     */
    public abstract void install(@NonNull ClassLoader classLoader);

    /**
     * Instantiates a new {@link Builder} instance.
     *
     * @param context the context
     * @return the {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull Context context) {
        return new Builder(context.getApplicationInfo()).classNameFilters(BuiltinClassNameFilter.INSTANCE);
    }

    /**
     * The builder for {@link DexOpener}.
     */
    public static final class Builder {

        private final List<ClassNameFilter> classNameFilters = new ArrayList<>();
        private final ApplicationInfo applicationInfo;

        Builder(ApplicationInfo applicationInfo) {
            this.applicationInfo = applicationInfo;
        }

        /**
         * Appends class name filters.
         *
         * @param filters the class name filter
         * @return this builder
         */
        @NonNull
        public Builder classNameFilters(@NonNull ClassNameFilter... filters) {
            for (ClassNameFilter f : filters) {
                if (f == null) {
                    throw new IllegalArgumentException("'filters' contains null");
                }
                classNameFilters.add(f);
            }
            return this;
        }

        /**
         * Instantiates a new {@link DexOpener} instance.
         *
         * @return the {@link DexOpener}
         */
        @NonNull
        public DexOpener build() {
            List<ClassNameFilter> filters = new ArrayList<>(classNameFilters);
            ClassNameFilter filter = new ClassNameFilters(Collections.unmodifiableList(filters));
            return new DexOpenerImpl(applicationInfo, filter, DexFileLoader.INSTANCE, DexClassFileFactory.INSTANCE);
        }

    }

}

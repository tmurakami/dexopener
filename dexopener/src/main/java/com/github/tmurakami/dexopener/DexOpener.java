package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

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
        return new Builder(context.getApplicationInfo());
    }

    /**
     * The builder for {@link DexOpener}.
     */
    public static final class Builder {

        private final ApplicationInfo applicationInfo;
        private ClassNameFilter classNameFilter = AcceptAll.INSTANCE;

        Builder(ApplicationInfo applicationInfo) {
            this.applicationInfo = applicationInfo;
        }

        /**
         * Throws an {@link UnsupportedOperationException}.
         *
         * @throws UnsupportedOperationException this method is deprecated
         * @deprecated use {@link #classNameFilter(ClassNameFilter)} instead.
         */
        @Deprecated
        @NonNull
        public Builder classNameFilters(@SuppressWarnings("unused") @NonNull ClassNameFilter... filters)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Use classNameFilter(ClassNameFilter) instead");
        }

        /**
         * Sets a {@link ClassNameFilter}.
         *
         * @param filter the {@link ClassNameFilter}
         * @return this builder
         */
        @NonNull
        public Builder classNameFilter(@NonNull ClassNameFilter filter) {
            classNameFilter = filter;
            return this;
        }

        /**
         * Instantiates a new {@link DexOpener} instance.
         *
         * @return the {@link DexOpener}
         */
        @NonNull
        public DexOpener build() {
            return new DexOpenerImpl(applicationInfo,
                                     new ClassNameFilterWrapper(classNameFilter),
                                     DexFileLoader.INSTANCE,
                                     DexClassFileFactory.INSTANCE);
        }

    }

}

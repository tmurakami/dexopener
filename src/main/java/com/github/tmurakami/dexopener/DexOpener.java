package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * This is an object that provides the ability to mock final classes and methods.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DexOpener {

    DexOpener() {
    }

    /**
     * Install this to the given instrumentation.
     * <p>
     * Must call this before calling {@link Instrumentation#newApplication(ClassLoader, String, Context)}.
     * If an Application instance has already been created, {@link IllegalStateException} will be thrown.
     *
     * @param instrumentation the instrumentation
     */
    public abstract void install(@NonNull Instrumentation instrumentation);

    /**
     * Instantiate a new {@link Builder} instance.
     *
     * @return the {@link Builder}
     */
    @NonNull
    public static Builder builder() {
        return newBuilder().classNameFilters(BuiltinClassNameFilter.INSTANCE);
    }

    /**
     * Instantiate a new {@link DexOpener} instance.
     * <p>
     * This is equivalent to the following code:
     * <pre>{@code builder().build()}</pre>
     *
     * @return the {@link DexOpener}
     */
    public static DexOpener create() {
        return builder().build();
    }

    private static Builder newBuilder() {
        return new DexOpenerBuilderImpl(DexFileLoader.INSTANCE, DexClassFileFactory.INSTANCE);
    }

    /**
     * The builder for {@link DexOpener}.
     */
    public interface Builder {

        /**
         * Add class name filters.
         *
         * @param filters the class name filter
         * @return this builder
         */
        @NonNull
        Builder classNameFilters(@NonNull ClassNameFilter... filters);

        /**
         * Instantiate a new {@link DexOpener} instance.
         *
         * @return the {@link DexOpener}
         */
        @NonNull
        DexOpener build();

    }

}

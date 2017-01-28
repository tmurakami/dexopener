package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * This is an object that provides the ability to mock final classes and methods.
 */
public abstract class DexOpener {

    DexOpener() {
    }

    /**
     * Install the ability to mock final classes and methods.
     * <p>
     * Must call this before calling {@link Instrumentation#newApplication(ClassLoader, String, Context)}.
     * If an Application instance has already been created, {@link IllegalStateException} will be thrown.
     */
    public abstract void install();

    /**
     * Instantiate a Builder instance.
     *
     * @return the builder
     */
    @NonNull
    public static Builder builder() {
        return new DexOpenerBuilderImpl().classNameFilters(BuiltinClassNameFilter.INSTANCE);
    }

    /**
     * Install the ability to mock final classes and methods.
     * <p>
     * This is equivalent to the following code:
     * <pre>{@code builder().build(instrumentation).install()}</pre>
     *
     * @param instrumentation the instrumentation
     * @see #install()
     */
    public static void install(@NonNull Instrumentation instrumentation) {
        builder().build(instrumentation).install();
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
         * Instantiate a {@link DexOpener} instance.
         *
         * @param instrumentation the instrumentation
         * @return the {@link DexOpener}
         */
        @NonNull
        DexOpener build(@NonNull Instrumentation instrumentation);

    }

}

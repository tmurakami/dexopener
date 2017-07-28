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
     * Provides the ability to mock final classes and methods.
     * <p>
     * This is equivalent to the following code:
     * <pre>{@code
     * Context context = instrumentation.getTargetContext();
     * builder(context).build().installTo(context.getClassLoader());
     * }</pre>
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
     * Provides the ability to mock final classes and methods.
     * After calling this method, you can mock classes loaded by the given class loader.
     * <p>
     * Note that final classes loaded before calling this cannot be mocked.
     *
     * @param classLoader the class loader
     */
    public abstract void installTo(@NonNull ClassLoader classLoader);

    /**
     * Instantiates a new {@link Builder} instance.
     * <p>
     * By default, mockable final classes and methods are restricted under the package obtained by
     * {@link Context#getPackageName()}.
     * To change this restriction, use {@link Builder#classNameFilter(ClassNameFilter)} or
     * {@link Builder#openIf(ClassNameFilter)}.
     *
     * @param context the context
     * @return the {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull Context context) {
        return new Builder(context,
                           new DexFileLoader(),
                           new DexClassFileFactory(),
                           openClassesBelongingTo(context.getPackageName()));
    }

    private static ClassNameFilter openClassesBelongingTo(String packageName) {
        final String packagePrefix = packageName + '.';
        return new ClassNameFilter() {
            @Override
            public boolean accept(@NonNull String className) {
                return className.startsWith(packagePrefix);
            }
        };
    }

    /**
     * The builder for {@link DexOpener}.
     */
    public static final class Builder {

        private final Context context;
        private final DexFileLoader dexFileLoader;
        private final DexClassFileFactory dexClassFileFactory;
        private ClassNameFilter classNameFilter;

        private Builder(Context context,
                        DexFileLoader dexFileLoader,
                        DexClassFileFactory dexClassFileFactory,
                        ClassNameFilter classNameFilter) {
            this.context = context;
            this.dexFileLoader = dexFileLoader;
            this.dexClassFileFactory = dexClassFileFactory;
            this.classNameFilter = classNameFilter;
        }

        /**
         * Sets a {@link ClassNameFilter}.
         * This is an alias of {@link #classNameFilter(ClassNameFilter)}. Using this makes code more
         * readable with lambda expressions, as in the following code:
         * <pre>{@code
         * openIf(name -> name.startsWith("package.you.want.to.mock."))
         * }</pre>
         *
         * @param filter the {@link ClassNameFilter}
         * @return this builder
         */
        @NonNull
        public Builder openIf(@NonNull ClassNameFilter filter) {
            return classNameFilter(filter);
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
            return new DexOpenerImpl(context,
                                     new ClassNameFilterWrapper(classNameFilter),
                                     dexFileLoader,
                                     dexClassFileFactory);
        }

    }

}

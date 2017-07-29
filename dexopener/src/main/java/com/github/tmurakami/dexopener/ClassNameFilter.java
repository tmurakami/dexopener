package com.github.tmurakami.dexopener;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * The class name filter.
 *
 * @see DexOpener.Builder#buildConfig(Class)
 * @deprecated If your app's root package is different from the value obtained by calling
 * {@link Context#getPackageName()}, Use {@link DexOpener.Builder#buildConfig(Class)}. This will be
 * removed in the future.
 */
@Deprecated
@SuppressWarnings({"WeakerAccess", "deprecation"})
public interface ClassNameFilter {

    /**
     * Tests whether to process the specified class.
     *
     * @param className the class name
     * @return {@code true} if the class should be processed; {@code false} otherwise
     */
    boolean accept(@NonNull String className);

}

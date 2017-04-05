package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

/**
 * The class name filter.
 */
@SuppressWarnings("WeakerAccess")
public interface ClassNameFilter {

    /**
     * Tests whether to process the specified class.
     *
     * @param className the class name
     * @return {@code true} if the class should be processed; {@code false} otherwise
     */
    boolean accept(@NonNull String className);

}

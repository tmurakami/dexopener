package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

final class ClassNameFilterWrapper implements ClassNameFilter {

    private static final String[] EXCLUDED_PACKAGES = {
            "android.",
            "com.android.",
            "com.github.tmurakami.classinjector.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "com.github.tmurakami.mockito4k.",
            "java.",
            "javax.",
            "junit.",
            "kotlin.",
            "kotlinx.",
            "net.bytebuddy.",
            "org.hamcrest.",
            "org.jacoco.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
    };

    private final ClassNameFilter delegate;

    ClassNameFilterWrapper(ClassNameFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean accept(@NonNull String className) {
        return isAcceptable(className) && delegate.accept(className);
    }

    private static boolean isAcceptable(String className) {
        // The Data Binding Library generates several classes packaged as 'android.databinding'.
        // Since these classes are tightly coupled with user classes, 'android.databinding' must not
        // be filtered out.
        if (className.startsWith("android.databinding.")) {
            // Built-in binding adapters should be excluded.
            return !className.startsWith("android.databinding.adapters.");
        }
        for (String pkg : EXCLUDED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        return !className.endsWith(".R")
                && !className.contains(".R$")
                && !className.endsWith(".BuildConfig")
                && !className.endsWith(".BR");
    }

}

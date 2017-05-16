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
        return isAccepted(className) && delegate.accept(className);
    }

    private static boolean isAccepted(String name) {
        // The Data Binding Library generates several classes packaged as 'android.databinding'.
        // Since these classes are tightly coupled with user classes, 'android.databinding' must not
        // be filtered out.
        if (name.startsWith("android.databinding.")) {
            return true;
        }
        for (String pkg : EXCLUDED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        return !name.endsWith(".R") && !name.contains(".R$") && !name.endsWith(".BuildConfig");
    }

}

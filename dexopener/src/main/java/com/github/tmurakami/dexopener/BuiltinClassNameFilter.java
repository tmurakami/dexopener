package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

final class BuiltinClassNameFilter implements ClassNameFilter {

    static final BuiltinClassNameFilter INSTANCE = new BuiltinClassNameFilter();

    private final String[] disallowedPackages = {
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

    private BuiltinClassNameFilter() {
    }

    @Override
    public boolean accept(@NonNull String className) {
        // The Data Binding Library generates several classes packaged as 'android.databinding'.
        // Since these classes are tightly coupled with user classes, 'android.databinding' must not
        // be filtered out.
        if (className.startsWith("android.databinding.")) {
            return true;
        }
        for (String pkg : disallowedPackages) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        return !className.endsWith(".R") && !className.contains(".R$") && !className.endsWith(".BuildConfig");
    }

}

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
            "java.",
            "javax.",
            "junit.",
            "kotlin.",
            "kotlinx.",
            "net.bytebuddy.",
            "org.hamcrest.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
    };

    private BuiltinClassNameFilter() {
    }

    @Override
    public boolean accept(@NonNull String className) {
        for (String pkg : disallowedPackages) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        return !className.endsWith(".R") && !className.contains(".R$") && !className.endsWith(".BuildConfig");
    }

}

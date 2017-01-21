package com.github.tmurakami.dexopener;

final class ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "android.",
            "com.android.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "junit.",
            "kotlin.",
            "kotlinx.",
            "net.bytebuddy.",
            "org.hamcrest.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
    };

    boolean accept(String className) {
        for (String pkg : IGNORED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        return !className.endsWith(".R") && !className.contains(".R$") && !className.endsWith(".BuildConfig");
    }

}

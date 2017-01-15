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

    boolean accept(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        return !name.endsWith(".R") && !name.contains(".R$");
    }

}

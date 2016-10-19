package com.github.tmurakami.dexopener;

final class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "android.databinding.",
            "android.support.",
            "com.android.dx.",
            "com.android.test.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "kotlin.",
            "net.bytebuddy.",
            "org.hamcrest.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
    };

    @Override
    public boolean accept(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        return !name.endsWith(".R") && !name.contains(".R$");
    }

}

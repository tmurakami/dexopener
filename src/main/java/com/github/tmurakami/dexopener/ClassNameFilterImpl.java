package com.github.tmurakami.dexopener;

final class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "Landroid/support/annotation/",
            "Landroid/support/multidex/",
            "Landroid/support/test/",
            "Lcom/android/dex/",
            "Lcom/android/dx/",
            "Lcom/android/internal/",
            "Lcom/github/tmurakami/dexmockito/",
            "Lcom/github/tmurakami/dexopener/",
            "Ljunit/",
            "Lnet/bytebuddy/",
            "Lorg/hamcrest/",
            "Lorg/junit/",
            "Lorg/mockito/",
            "Lorg/objenesis/",
    };

    @Override
    public boolean accept(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        String n = name.substring(0, name.length() - 1);
        int dollar = n.lastIndexOf('$');
        if (dollar > -1) {
            n = n.substring(0, dollar);
        }
        return !n.endsWith("/R") && !n.endsWith("/BuildConfig");
    }

}

package com.github.tmurakami.dexopener;

class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "java.",
            "javax.",
            "dalvik.",
            "junit.",
            "org.junit.",
            "org.hamcrest.",
            "android.support.annotation.",
            "android.support.multidex.",
            "android.support.test.",
            "org.mockito.",
            "net.bytebuddy.",
            "org.objenesis.",
            "com.android.dx.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
    };

    @Override
    public boolean accept(String className) {
        for (String pkg : IGNORED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        return true;
    }

}

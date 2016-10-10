package com.github.tmurakami.dexopener;

class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "android.support.annotation.",
            "android.support.multidex.",
            "android.support.test.",
            "com.android.internal.util.",
            "com.android.dx.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "com.sun.",
            "dalvik.",
            "java.",
            "javax.",
            "junit.",
            "net.bytebuddy.",
            "org.apache.http.",
            "org.apache.harmony.dalvik.",
            "org.hamcrest.",
            "org.json.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
            "org.w3c.dom.",
            "org.xml.sax.",
            "org.xmlpull.v1.",
    };

    @Override
    public boolean accept(String className) {
        for (String pkg : IGNORED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        if (className.startsWith("android.") && !className.startsWith("android.support.")) {
            return false;
        }
        int dollar = className.indexOf('$');
        if (dollar == -1) {
            return true;
        }
        String name = className.substring(0, dollar);
        return !name.endsWith(".BuildConfig") && !name.endsWith(".R");
    }

}

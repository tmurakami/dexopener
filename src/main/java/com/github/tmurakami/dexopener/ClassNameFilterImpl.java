package com.github.tmurakami.dexopener;

class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "android.support.annotation.",
            "android.support.multidex.",
            "android.support.test.",
            "com.android.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "com.sun.",
            "dalvik.",
            "java.",
            "javax.",
            "libcore.",
            "junit.",
            "net.bytebuddy.",
            "org.apache.harmony.dalvik.",
            "org.apache.http.",
            "org.ccil.cowan.tagsoup.",
            "org.hamcrest.",
            "org.json.",
            "org.kxml2.io.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
            "org.w3c.dom.",
            "org.xml.sax.",
            "org.xmlpull.v1.",
            "sun.",
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
        String name = className;
        int dollar = className.indexOf('$');
        if (dollar > -1) {
            name = name.substring(0, dollar);
        }
        return !name.endsWith(".BuildConfig") && !name.endsWith(".R");
    }

}

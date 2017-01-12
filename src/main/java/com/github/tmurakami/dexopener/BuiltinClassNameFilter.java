package com.github.tmurakami.dexopener;

final class BuiltinClassNameFilter implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "android.",
            "com.android.",
            "com.github.tmurakami.dexopener.",
            "com.google.android.collect.",
            "com.google.android.gles_jni.",
            "com.ibm.icu4jni.",
            "dalvik.",
            "java.",
            "javax.",
            "junit.",
            "kotlin.",
            "kotlinx.",
            "libcore.",
            "org.apache.commons.logging.",
            "org.apache.harmony.",
            "org.apache.http.",
            "org.bouncycastle.",
            "org.ccil.cowan.tagsoup.",
            "org.hamcrest.",
            "org.json.",
            "org.junit.",
            "org.kxml2.io.",
            "org.w3c.dom.",
            "org.xml.sax.",
            "org.xmlpull.v1.",
            "sun.",
    };

    private final ClassNameFilter delegate;

    BuiltinClassNameFilter(ClassNameFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean accept(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        return delegate.accept(name);
    }

}

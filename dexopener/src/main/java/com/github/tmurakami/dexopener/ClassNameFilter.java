package com.github.tmurakami.dexopener;

final class ClassNameFilter {

    private static final String[] INCLUDED_PACKAGES = {
            "android.databinding.generated.",
    };

    private static final String[] INCLUDED_CLASSES = {
            // Since the Data Binding Library generates several classes that are tightly coupled
            // with user classes, do not exclude the following classes.
            "android.databinding.DataBinderMapper",
            "android.databinding.DataBindingComponent",
            "android.databinding.DataBindingUtil",
    };

    private static final String[] EXCLUDED_PACKAGES = {
            "android.",
            "com.android.",
            "com.github.tmurakami.classinjector.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "com.github.tmurakami.mockito4k.",
            "dalvik.",
            "java.",
            "javax.",
            "junit.",
            "kotlin.",
            "kotlinx.",
            "net.bytebuddy.",
            "org.apache.http.",
            "org.hamcrest.",
            "org.jacoco.",
            "org.json.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
            "org.w3c.dom.",
            "org.xml.sax.",
            "org.xmlpull.v1.",
    };

    private final String packagePrefix;

    ClassNameFilter(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    boolean accept(String className) {
        for (String pkg : INCLUDED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        for (String cls : INCLUDED_CLASSES) {
            if (className.equals(cls)) {
                return true;
            }
        }
        for (String pkg : EXCLUDED_PACKAGES) {
            if (className.startsWith(pkg)) {
                return false;
            }
        }
        return !className.endsWith(".R")
                && !className.contains(".R$")
                && !className.endsWith(".BuildConfig")
                && !className.endsWith(".BR")
                && className.startsWith(packagePrefix);
    }

}

package com.github.tmurakami.dexopener;

import java.util.StringTokenizer;

final class ClassNameFilterImpl implements ClassNameFilter {

    private static final String[] IGNORED_PACKAGES = {
            "android.support.annotation.",
            "android.support.multidex.",
            "android.support.test.",
            "com.android.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
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
    public boolean accept(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        if (name.startsWith("android.") && !name.startsWith("android.support.")) {
            return false;
        }
        String s = name;
        int dot = s.lastIndexOf('.');
        if (dot > -1) {
            s = s.substring(dot + 1);
        }
        boolean first = true;
        for (StringTokenizer t = new StringTokenizer(s, "$"); t.hasMoreTokens(); ) {
            String token = t.nextToken();
            if (first && (token.equals("R") || token.equals("BuildConfig")) || token.startsWith("zz")) {
                return false;
            }
            first = false;
        }
        return true;
    }

}

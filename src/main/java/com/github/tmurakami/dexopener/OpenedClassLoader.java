package com.github.tmurakami.dexopener;

final class OpenedClassLoader extends ClassLoader {

    private static final String[] IGNORED_PACKAGES = {
            "android.support.annotation.",
            "android.support.multidex.",
            "android.support.test.",
            "com.android.dx.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
            "net.bytebuddy.",
            "org.hamcrest.",
            "org.junit.",
            "org.mockito.",
            "org.objenesis.",
    };

    private final ClassLoader classLoader;
    private final Iterable<DexElement> elements;

    OpenedClassLoader(ClassLoader classLoader, Iterable<DexElement> elements) {
        super(classLoader.getParent());
        this.classLoader = classLoader;
        this.elements = elements;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (shouldLoad(name)) {
            for (DexElement e : elements) {
                Class<?> c = e.loadClass(name, classLoader);
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }

    private static boolean shouldLoad(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        String s = name;
        int dollar = s.indexOf('$');
        if (dollar > -1) {
            s = s.substring(0, dollar);
        }
        return !s.endsWith(".BuildConfig") && !s.endsWith(".R");
    }

}

package com.github.tmurakami.dexopener;

final class InternalClassLoader extends ClassLoader {

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
    private final Iterable<Dex> dexes;
    private SuperCalls superCalls = new SuperCalls() {
        @Override
        public Class findClass(String name) throws ClassNotFoundException {
            return InternalClassLoader.super.findClass(name);
        }
    };

    InternalClassLoader(ClassLoader classLoader, Iterable<Dex> dexes) {
        super(classLoader.getParent());
        this.classLoader = classLoader;
        this.dexes = dexes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (shouldLoad(name)) {
            for (Dex d : dexes) {
                Class<?> c = d.loadClass(name, classLoader);
                if (c != null) {
                    return c;
                }
            }
        }
        return superCalls.findClass(name);
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

    interface SuperCalls {
        Class findClass(String name) throws ClassNotFoundException;
    }

}

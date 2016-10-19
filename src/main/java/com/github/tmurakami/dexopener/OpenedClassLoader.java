package com.github.tmurakami.dexopener;

final class OpenedClassLoader extends ClassLoader {

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
        return super.findClass(name);
    }

    private static boolean shouldLoad(String name) {
        for (String pkg : IGNORED_PACKAGES) {
            if (name.startsWith(pkg)) {
                return false;
            }
        }
        return !name.endsWith(".R") && !name.contains(".R$");
    }

}

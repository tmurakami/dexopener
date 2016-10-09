package com.github.tmurakami.dexopener;

import dalvik.system.DexClassLoader;

final class InternalClassLoader extends DexClassLoader {

    private final ClassNameFilter classNameFilter;

    InternalClassLoader(String dexPath,
                        String optimizedDirectory,
                        ClassNameFilter classNameFilter,
                        ClassLoader parent) {
        super(dexPath, optimizedDirectory, null, parent);
        this.classNameFilter = classNameFilter;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (name.intern()) {
            Class<?> c = findLoadedClass(name);
            if (c == null && !classNameFilter.accept(name)) {
                c = getParent().loadClass(name);
            }
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (c == null) {
                c = getParent().loadClass(name);
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

}

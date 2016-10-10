package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexClassLoader;

final class ClassLoaderFactoryImpl implements ClassLoaderFactory {

    private final ClassNameFilter classNameFilter;

    ClassLoaderFactoryImpl(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    @Override
    public ClassLoader newClassLoader(ClassLoader parent,
                                      File optimizedDirectory,
                                      String... dexPaths) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (String path : dexPaths) {
            if (path.length() > 0) {
                builder.append(':');
            }
            builder.append(path);
        }
        return new InternalClassLoader(builder.toString(), optimizedDirectory.getCanonicalPath(), classNameFilter, parent);
    }

    private static class InternalClassLoader extends DexClassLoader {

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

}

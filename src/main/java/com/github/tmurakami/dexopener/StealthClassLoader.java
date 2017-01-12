package com.github.tmurakami.dexopener;

import java.io.IOException;

final class StealthClassLoader extends ClassLoader {

    private final ClassLoader classLoader;
    private final ClassNameFilter classNameFilter;
    private final Iterable<DexElement> elements;

    StealthClassLoader(ClassLoader classLoader,
                       ClassNameFilter classNameFilter,
                       Iterable<DexElement> elements) {
        super(classLoader.getParent());
        this.classLoader = classLoader;
        this.classNameFilter = classNameFilter;
        this.elements = elements;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classNameFilter.accept(name)) {
            for (DexElement d : elements) {
                Class<?> c;
                try {
                    c = d.loadClass(name, classLoader);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
                if (c != null) {
                    return c;
                }
            }
        }
        return super.findClass(name);
    }

}

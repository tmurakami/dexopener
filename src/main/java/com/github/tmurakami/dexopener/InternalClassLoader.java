package com.github.tmurakami.dexopener;

import java.io.IOException;
import java.util.List;

final class InternalClassLoader extends ClassLoader {

    private final ClassLoader classLoader;
    private final List<Dex> dices;
    private final ClassNameFilter classNameFilter;

    InternalClassLoader(ClassLoader classLoader, List<Dex> dices, ClassNameFilter classNameFilter) {
        super(classLoader.getParent());
        this.classLoader = classLoader;
        this.dices = dices;
        this.classNameFilter = classNameFilter;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (name.intern()) {
            if (classNameFilter.accept(name)) {
                return findClass(name);
            } else {
                return getParent().loadClass(name);
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (Dex d : dices) {
            try {
                Class<?> c = d.loadClass(name, classLoader);
                if (c != null) {
                    return c;
                }
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
        return null;
    }

}

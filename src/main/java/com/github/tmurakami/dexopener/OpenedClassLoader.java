package com.github.tmurakami.dexopener;

final class OpenedClassLoader extends ClassLoader {

    private final ClassLoader classLoader;
    private final ClassNameFilter classNameFilter;
    private final Iterable<DexElement> elements;

    OpenedClassLoader(ClassLoader classLoader, ClassNameFilter classNameFilter, Iterable<DexElement> elements) {
        super(classLoader.getParent());
        this.classLoader = classLoader;
        this.classNameFilter = classNameFilter;
        this.elements = elements;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classNameFilter.accept(name)) {
            for (DexElement e : elements) {
                Class<?> c = e.loadClass(name, classLoader);
                if (c != null) {
                    return c;
                }
            }
        }
        return super.findClass(name);
    }

}

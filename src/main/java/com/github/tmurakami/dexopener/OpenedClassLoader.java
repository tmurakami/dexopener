package com.github.tmurakami.dexopener;

final class OpenedClassLoader extends ClassLoader {

    private final ClassLoader classLoader;
    private final ClassNameFilter classNameFilter;
    private final DexElement element;

    OpenedClassLoader(ClassLoader classLoader, ClassNameFilter classNameFilter, DexElement element) {
        super(classLoader.getParent());
        this.classLoader = classLoader;
        this.classNameFilter = classNameFilter;
        this.element = element;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classNameFilter.accept(name)) {
            Class<?> c = element.loadClass(name, classLoader);
            if (c != null) {
                return c;
            }
        }
        return super.findClass(name);
    }

}

package com.github.tmurakami.dexopener;

final class ClassLoaderFactory {

    private final ClassNameFilter classNameFilter;

    ClassLoaderFactory(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    ClassLoader newClassLoader(ClassLoader classLoader, Iterable<DexElement> elements) {
        return new StealthClassLoader(classLoader, classNameFilter, elements);
    }

}

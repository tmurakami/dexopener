package com.github.tmurakami.dexopener;

final class DexElements implements DexElement {

    private final Iterable<DexElement> elements;

    DexElements(Iterable<DexElement> elements) {
        this.elements = elements;
    }

    @Override
    public Class loadClass(String name, ClassLoader classLoader) {
        for (DexElement e : elements) {
            Class<?> c = e.loadClass(name, classLoader);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

}

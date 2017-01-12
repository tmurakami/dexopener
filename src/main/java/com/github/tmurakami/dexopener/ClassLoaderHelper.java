package com.github.tmurakami.dexopener;

import java.lang.reflect.Field;

final class ClassLoaderHelper {
    void setParent(ClassLoader classLoader, ClassLoader parent) {
        Field f;
        try {
            f = ClassLoader.class.getDeclaredField("parent");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
        while (true) {
            f.setAccessible(true);
            try {
                f.set(classLoader, parent);
                return;
            } catch (IllegalAccessException ignored) {
            }
        }
    }
}

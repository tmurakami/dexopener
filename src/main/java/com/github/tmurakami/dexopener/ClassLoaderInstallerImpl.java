package com.github.tmurakami.dexopener;

import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.DexFile;

final class ClassLoaderInstallerImpl implements ClassLoaderInstaller {

    private static final Field PARENT;

    static {
        try {
            PARENT = ClassLoader.class.getDeclaredField("parent");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    @Override
    public void install(ClassLoader classLoader, List<DexFile> dexFiles) {
        ClassLoader loader = new InternalClassLoader(classLoader, dexFiles);
        while (true) {
            PARENT.setAccessible(true);
            try {
                PARENT.set(classLoader, loader);
                return;
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static class InternalClassLoader extends ClassLoader {

        private final ClassLoader classLoader;
        private final List<DexFile> dexFiles;

        InternalClassLoader(ClassLoader classLoader, List<DexFile> dexFiles) {
            super(classLoader.getParent());
            this.classLoader = classLoader;
            this.dexFiles = dexFiles;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (name.intern()) {
                Class<?> c = findLoadedClass(name);
                if (c != null) {
                    return c;
                }
                for (DexFile f : dexFiles) {
                    c = f.loadClass(name, classLoader);
                    if (c != null) {
                        return c;
                    }
                }
                return getParent().loadClass(name);
            }
        }

    }

}

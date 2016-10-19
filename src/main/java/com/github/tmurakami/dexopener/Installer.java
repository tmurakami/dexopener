package com.github.tmurakami.dexopener;

import android.content.Context;

import java.io.IOException;

import dalvik.system.DexFile;

abstract class Installer {

    abstract void install(Context context);

    static Installer create() {
        DexElementFactory elementFactory = new DexElementFactoryImpl(newDexFileLoader());
        ClassLoaderFactory classLoaderFactory = newClassLoaderFactory(new ClassNameFilterImpl());
        return new InstallerImpl(new MultiDexHelperImpl(), elementFactory, classLoaderFactory, new ClassLoaderHelperImpl());
    }

    private static DexFileLoader newDexFileLoader() {
        return new DexFileLoader() {
            @Override
            public DexFile load(String sourcePathName, String outputPathName) throws IOException {
                return DexFile.loadDex(sourcePathName, outputPathName, 0);
            }
        };
    }

    private static ClassLoaderFactory newClassLoaderFactory(final ClassNameFilter classNameFilter) {
        return new ClassLoaderFactory() {
            @Override
            public ClassLoader newClassLoader(ClassLoader classLoader, Iterable<DexElement> elements) {
                return new OpenedClassLoader(classLoader, classNameFilter, new DexElements(elements));
            }
        };
    }

}

package com.github.tmurakami.dexopener;

import android.content.Context;

import java.io.IOException;

import dalvik.system.DexFile;

abstract class Installer {

    abstract void install(Context context);

    static Installer create() {
        ClassNameFilter classNameFilter = new ClassNameFilterImpl();
        DexElementFactory elementFactory = new DexElementFactoryImpl(classNameFilter, newDexFileLoader());
        ClassLoaderFactory classLoaderFactory = newClassLoaderFactory(classNameFilter);
        return new InstallerImpl(elementFactory, classLoaderFactory, new ClassLoaderHelperImpl());
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
            public ClassLoader newClassLoader(ClassLoader classLoader, DexElement element) {
                return new OpenedClassLoader(classLoader, classNameFilter, element);
            }
        };
    }

}

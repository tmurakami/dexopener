package com.github.tmurakami.dexopener;

import android.content.Context;

import java.io.IOException;

import dalvik.system.DexFile;

abstract class DexInstaller {

    abstract void install(Context context);

    static DexInstaller create() {
        DexFactory dexFactory = new DexFactoryImpl(newDexFileLoader());
        ClassLoaderFactory classLoaderFactory = newClassLoaderFactory();
        return new DexInstallerImpl(new MultiDexHelperImpl(), dexFactory, classLoaderFactory, new ClassLoaderHelperImpl());
    }

    private static DexFileLoader newDexFileLoader() {
        return new DexFileLoader() {
            @Override
            public DexFile load(String sourcePathName, String outputPathName) throws IOException {
                return DexFile.loadDex(sourcePathName, outputPathName, 0);
            }
        };
    }

    private static ClassLoaderFactory newClassLoaderFactory() {
        return new ClassLoaderFactory() {
            @Override
            public ClassLoader newClassLoader(ClassLoader classLoader, Iterable<Dex> dexes) {
                return new InternalClassLoader(classLoader, dexes);
            }
        };
    }

}

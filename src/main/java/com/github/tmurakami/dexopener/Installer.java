package com.github.tmurakami.dexopener;

import android.content.Context;

import java.io.IOException;

import dalvik.system.DexFile;

abstract class Installer {

    abstract void install(Context context);

    static Installer create() {
        ClassNameFilter classNameFilter = new ClassNameFilterImpl();
        DexFileLoader fileLoader = newDexFileLoader();
        ClassNameReaderImpl classNameReader = new ClassNameReaderImpl(classNameFilter);
        DexFileGenerator fileGenerator = new DexFileGeneratorImpl(fileLoader);
        DexElementFactory elementFactory = new DexElementFactoryImpl(classNameReader, fileGenerator);
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
            public ClassLoader newClassLoader(ClassLoader classLoader, Iterable<DexElement> elements) {
                return new OpenedClassLoader(classLoader, classNameFilter, elements);
            }
        };
    }

}

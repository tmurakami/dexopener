package com.github.tmurakami.dexopener;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dalvik.system.DexFile;

abstract class DexInstaller {

    abstract void install(Context context);

    static DexInstaller create() {
        DexFactory dexFactory = newDexFactory(Executors.newCachedThreadPool(), newDexFileLoader());
        ClassLoaderFactory classLoaderFactory = newClassLoaderFactory(new ClassNameFilterImpl());
        return new DexInstallerImpl(new MultiDexHelperImpl(), dexFactory, classLoaderFactory, new ClassLoaderHelperImpl());
    }

    private static DexFactory newDexFactory(final ExecutorService executorService, final DexFileLoader fileLoader) {
        return new DexFactory() {
            @Override
            public Dex newDex(File file, File cacheDir) {
                return new DexImpl(executorService.submit(new ApplicationReaderTask(file)), cacheDir, fileLoader);
            }
        };
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
            public ClassLoader newClassLoader(ClassLoader classLoader, List<Dex> dices) {
                return new InternalClassLoader(classLoader, dices, classNameFilter);
            }
        };
    }

}

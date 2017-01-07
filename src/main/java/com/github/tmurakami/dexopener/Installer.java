package com.github.tmurakami.dexopener;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import dalvik.system.DexFile;

abstract class Installer {

    abstract void install(Context context);

    static Installer create() {
        ClassNameFilter classNameFilter = new ClassNameFilterImpl();
        DexFileLoader fileLoader = newDexFileLoader();
        ClassNameReaderImpl classNameReader = new ClassNameReaderImpl(classNameFilter);
        DexFileGenerator fileGenerator = new DexFileGeneratorImpl(fileLoader);
        ExecutorService executorService = newExecutorService();
        DexElementFactory elementFactory = new DexElementFactoryImpl(classNameReader, fileGenerator, executorService);
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

    private static ExecutorService newExecutorService() {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "DexMockito");
            }
        });
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

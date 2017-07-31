package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.concurrent.Executor;

@SuppressWarnings("deprecation")
final class AndroidClassSourceFactory {

    private final ClassNameFilter classNameFilter;
    private final Executor executor;

    AndroidClassSourceFactory(ClassNameFilter classNameFilter, Executor executor) {
        this.classNameFilter = classNameFilter;
        this.executor = executor;
    }

    ClassSource newClassSource(String sourceDir, File cacheDir) {
        return new AndroidClassSource(sourceDir,
                                      classNameFilter,
                                      newDexFileHolderMapper(cacheDir),
                                      new DexClassSourceFactory());
    }

    private DexFileHolderMapper newDexFileHolderMapper(File cacheDir) {
        return new DexFileHolderMapper(classNameFilter, executor, new DexFileTaskFactory(cacheDir));
    }

}

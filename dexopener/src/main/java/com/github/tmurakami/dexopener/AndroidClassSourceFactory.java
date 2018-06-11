package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class AndroidClassSourceFactory {

    private static final Executor EXECUTOR;

    static {
        final AtomicInteger count = new AtomicInteger();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int nThreads = Math.max(1, Math.min(availableProcessors, 4)); // 1 to 4
        EXECUTOR = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "DexOpener #" + count.incrementAndGet());
            }
        });
    }

    private final ClassNameFilter classNameFilter;

    AndroidClassSourceFactory(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    ClassSource newClassSource(String sourceDir, File cacheDir) {
        return new AndroidClassSource(sourceDir,
                                      classNameFilter,
                                      newDexFileHolderMapper(cacheDir),
                                      new DexClassSourceFactory());
    }

    private DexFileHolderMapper newDexFileHolderMapper(File cacheDir) {
        return new DexFileHolderMapper(classNameFilter, new DexFileGenerator(EXECUTOR, cacheDir));
    }

}

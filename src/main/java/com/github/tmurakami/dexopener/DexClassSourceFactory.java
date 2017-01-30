package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.util.Set;

final class DexClassSourceFactory {

    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexClassSourceFactory(File cacheDir,
                          DexFileLoader dexFileLoader,
                          DexClassFileFactory dexClassFileFactory) {
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    ClassSource newClassSource(ApplicationReader applicationReader, Set<String> classNames) {
        return new DexClassSource(applicationReader, classNames, cacheDir, dexFileLoader, dexClassFileFactory);
    }

}

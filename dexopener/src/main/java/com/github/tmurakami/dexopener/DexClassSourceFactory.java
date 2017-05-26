package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

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

    ClassSource newClassSource(byte[] byteCode, Set<Set<String>> internalNamesSet) {
        return new DexClassSource(byteCode,
                                  new HashSet<>(internalNamesSet),
                                  new HashMap<String, DexFile>(),
                                  cacheDir,
                                  dexFileLoader,
                                  dexClassFileFactory);
    }

}

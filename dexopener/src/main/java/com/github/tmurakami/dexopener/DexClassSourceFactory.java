package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

final class DexClassSourceFactory {

    private static final int MAX_SIZE_PER_NAMES = 150;

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

    ClassSource newClassSource(byte[] byteCode, Set<String> internalNames) {
        return new DexClassSource(byteCode,
                                  toInternalNamesSet(internalNames),
                                  new HashMap<String, DexFile>(),
                                  cacheDir,
                                  dexFileLoader,
                                  dexClassFileFactory);
    }

    private static Set<Set<String>> toInternalNamesSet(Set<String> internalNames) {
        List<String> list = new ArrayList<>(internalNames);
        Collections.sort(list);
        Set<Set<String>> internalNamesSet = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (String name : list) {
            names.add(name);
            if (names.size() == MAX_SIZE_PER_NAMES) {
                internalNamesSet.add(Collections.unmodifiableSet(names));
                names = new HashSet<>();
            }
        }
        if (!names.isEmpty()) {
            internalNamesSet.add(Collections.unmodifiableSet(names));
        }
        return internalNamesSet;
    }

}

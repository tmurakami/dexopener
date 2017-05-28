package com.github.tmurakami.dexopener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

final class DexFilesFactory {

    private static final int MAX_SIZE_PER_NAMES = 150;

    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFilesFactory(File cacheDir, DexFileLoader dexFileLoader) {
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    DexFiles newDexFiles(byte[] byteCode, Set<String> classNames) {
        return new DexFiles(byteCode,
                            toInternalNamesSet(classNames),
                            cacheDir,
                            dexFileLoader,
                            new HashMap<String, DexFile>());
    }

    private static Set<Set<String>> toInternalNamesSet(Set<String> classNames) {
        List<String> list = new ArrayList<>(classNames);
        Collections.sort(list);
        Set<Set<String>> internalNamesSet = new HashSet<>();
        Set<String> internalNames = new HashSet<>();
        for (String name : list) {
            internalNames.add(TypeUtils.getInternalName(name));
            if (internalNames.size() == MAX_SIZE_PER_NAMES) {
                internalNamesSet.add(Collections.unmodifiableSet(internalNames));
                internalNames = new HashSet<>();
            }
        }
        if (!internalNames.isEmpty()) {
            internalNamesSet.add(Collections.unmodifiableSet(internalNames));
        }
        return internalNamesSet;
    }

}

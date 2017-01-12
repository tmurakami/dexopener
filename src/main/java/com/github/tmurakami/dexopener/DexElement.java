package com.github.tmurakami.dexopener;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import dalvik.system.DexFile;

final class DexElement {

    private final DexFileGenerator dexFileGenerator;
    private final Iterable<Set<String>> classNamesSet;
    private final ConcurrentMap<Set<String>, DexFile> dexFileMap;

    DexElement(DexFileGenerator dexFileGenerator,
               Iterable<Set<String>> classNamesSet,
               ConcurrentMap<Set<String>, DexFile> dexFileMap) {
        this.dexFileGenerator = dexFileGenerator;
        this.classNamesSet = classNamesSet;
        this.dexFileMap = dexFileMap;
    }

    Class loadClass(String name, ClassLoader classLoader) throws IOException {
        String className = 'L' + name.replace('.', '/') + ';';
        for (Set<String> names : classNamesSet) {
            if (names.contains(className)) {
                DexFile dexFile = dexFileMap.get(names);
                if (dexFile == null) {
                    DexFile newDexFile = dexFileGenerator.generate(names);
                    if ((dexFile = dexFileMap.putIfAbsent(names, newDexFile)) == null) {
                        dexFile = newDexFile;
                    }
                }
                return dexFile.loadClass(name, classLoader);
            }
        }
        return null;
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import dalvik.system.DexFile;

final class DexElement {

    private final ApplicationReader ar;
    private final File cacheDir;
    private final DexFileGenerator fileGenerator;
    private final Set<Set<String>> classNamesSet;
    private final ConcurrentMap<Set<String>, DexFile> dexFileMap;

    DexElement(ApplicationReader ar,
               File cacheDir,
               DexFileGenerator fileGenerator,
               Set<Set<String>> classNamesSet,
               ConcurrentMap<Set<String>, DexFile> dexFileMap) {
        this.ar = ar;
        this.cacheDir = cacheDir;
        this.fileGenerator = fileGenerator;
        this.classNamesSet = classNamesSet;
        this.dexFileMap = dexFileMap;
    }

    Class loadClass(String name, ClassLoader classLoader) {
        String className = 'L' + name.replace('.', '/') + ';';
        for (Set<String> names : classNamesSet) {
            if (names.contains(className)) {
                DexFile dexFile = dexFileMap.get(names);
                if (dexFile == null) {
                    DexFile newDexFile = fileGenerator.generateDexFile(ar, cacheDir, names);
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

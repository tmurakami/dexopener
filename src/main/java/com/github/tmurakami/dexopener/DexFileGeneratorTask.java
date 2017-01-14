package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import dalvik.system.DexFile;

final class DexFileGeneratorTask implements Runnable {

    private final ApplicationReader ar;
    private final File cacheDir;
    private final DexFileGenerator fileGenerator;
    private final Set<Set<String>> classNameSet;
    private final ConcurrentMap<Set<String>, DexFile> dexFileMap;

    DexFileGeneratorTask(ApplicationReader ar,
                         File cacheDir,
                         DexFileGenerator fileGenerator,
                         Set<Set<String>> classNamesSet,
                         ConcurrentMap<Set<String>, DexFile> dexFileMap) {
        this.ar = ar;
        this.cacheDir = cacheDir;
        this.fileGenerator = fileGenerator;
        this.classNameSet = classNamesSet;
        this.dexFileMap = dexFileMap;
    }

    @Override
    public void run() {
        for (Set<String> names : classNameSet) {
            if (!dexFileMap.containsKey(names)) {
                dexFileMap.putIfAbsent(names, fileGenerator.generateDexFile(ar, cacheDir, names));
            }
        }
    }

}

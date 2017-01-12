package com.github.tmurakami.dexopener;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import dalvik.system.DexFile;

final class DexFileGeneratorTask implements Runnable {

    private final DexFileGenerator dexFileGenerator;
    private final Iterable<Set<String>> classNamesSet;
    private final ConcurrentMap<Set<String>, DexFile> dexFileMap;

    DexFileGeneratorTask(DexFileGenerator dexFileGenerator,
                         Iterable<Set<String>> classNamesSet,
                         ConcurrentMap<Set<String>, DexFile> dexFileMap) {
        this.dexFileGenerator = dexFileGenerator;
        this.classNamesSet = classNamesSet;
        this.dexFileMap = dexFileMap;
    }

    @Override
    public void run() {
        for (Set<String> names : classNamesSet) {
            if (!dexFileMap.containsKey(names)) {
                try {
                    dexFileMap.putIfAbsent(names, dexFileGenerator.generate(names));
                } catch (IOException e) {
                    Logger.getLogger("com.github.tmurakami.dexopener").log(Level.SEVERE, "Cannot generate dex file", e);
                    return;
                }
            }
        }
    }

}

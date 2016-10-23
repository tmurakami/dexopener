package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dalvik.system.DexFile;

final class DexElementImpl implements DexElement {

    private static final String[] EMPTY_STRINGS = new String[0];
    private static final int CLASSES_PER_DEX_FILE = 100;

    private final ApplicationReader ar;
    private final File cacheDir;
    private final DexFileGenerator fileGenerator;
    private final List<String> unloadedClassNames;
    private final List<DexFile> dexFiles = new ArrayList<>();

    DexElementImpl(ApplicationReader ar,
                   Collection<String> classNames,
                   File cacheDir,
                   DexFileGenerator fileGenerator) {
        this.ar = ar;
        this.cacheDir = cacheDir;
        this.fileGenerator = fileGenerator;
        List<String> names = new ArrayList<>(classNames);
        Collections.sort(names);
        this.unloadedClassNames = names;
    }

    @Override
    public Class loadClass(String name, ClassLoader classLoader) {
        for (DexFile d : dexFiles) {
            Class<?> c = d.loadClass(name, classLoader);
            if (c != null) {
                return c;
            }
        }
        String[] classesToVisit = findClassesToVisit(name, unloadedClassNames);
        if (classesToVisit.length == 0) {
            return null;
        }
        DexFile dexFile;
        try {
            dexFile = fileGenerator.generateDexFile(ar, cacheDir, classesToVisit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dexFiles.add(dexFile);
        return dexFile.loadClass(name, classLoader);
    }

    private static String[] findClassesToVisit(String name, List<String> unloadedClassNames) {
        String className = 'L' + name.replace('.', '/') + ';';
        int from = Collections.binarySearch(unloadedClassNames, className);
        if (from < 0) {
            return EMPTY_STRINGS;
        }
        int to = Math.min(from + CLASSES_PER_DEX_FILE, unloadedClassNames.size());
        List<String> names = new ArrayList<>(unloadedClassNames.subList(from, to));
        unloadedClassNames.removeAll(names);
        return names.toArray(new String[names.size()]);
    }

}

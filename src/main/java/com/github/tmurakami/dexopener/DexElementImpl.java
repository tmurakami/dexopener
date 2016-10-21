package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

final class DexElementImpl implements DexElement {

    private static final String[] EMPTY_STRINGS = new String[0];
    private static final int CLASSES_PER_DEX_FILE = 100;

    private final ApplicationReader ar;
    private final File cacheDir;
    private final DexGenerator dexGenerator;
    private final DexFileLoader fileLoader;
    private final List<String> unloadedClassNames;
    private final List<DexFile> dexFiles = new ArrayList<>();

    DexElementImpl(ApplicationReader ar,
                   Collection<String> classNames,
                   File cacheDir,
                   DexGenerator dexGenerator,
                   DexFileLoader fileLoader) {
        this.ar = ar;
        this.cacheDir = cacheDir;
        this.dexGenerator = dexGenerator;
        this.unloadedClassNames = new ArrayList<>(classNames);
        this.fileLoader = fileLoader;
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
            File source = dexGenerator.generateDexFile(ar, cacheDir, classesToVisit);
            File output = new File(cacheDir, source.getName() + ".dex");
            dexFile = fileLoader.load(source.getCanonicalPath(), output.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dexFiles.add(dexFile);
        return dexFile.loadClass(name, classLoader);
    }

    private static String[] findClassesToVisit(String name, Collection<String> unloadedClassNames) {
        String className = 'L' + name.replace('.', '/') + ';';
        if (!unloadedClassNames.contains(className)) {
            return EMPTY_STRINGS;
        }
        Set<String> names = new HashSet<>();
        names.add(className);
        int slash = className.lastIndexOf('/');
        String pkg = slash == -1 ? null : className.substring(0, slash) + '/';
        for (Iterator<String> it = unloadedClassNames.iterator(); it.hasNext(); ) {
            String s = it.next();
            if (pkg == null || s.startsWith(pkg)) {
                names.add(s);
                it.remove();
                if (names.size() > CLASSES_PER_DEX_FILE) {
                    break;
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

}

package com.github.tmurakami.dexopener;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

final class ClassNameReader {

    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final ClassNameFilter classNameFilter;

    ClassNameReader(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    Iterable<Set<String>> read(DexFile dexFile) {
        Set<Set<String>> classNamesSet = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (Enumeration<String> e = dexFile.entries(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            if (classNameFilter.accept(name)) {
                names.add('L' + name.replace('.', '/') + ';');
                if (names.size() >= MAX_CLASSES_PER_DEX_FILE) {
                    classNamesSet.add(names);
                    names = new HashSet<>();
                }
            }
        }
        classNamesSet.add(names);
        return Collections.unmodifiableSet(classNamesSet);
    }

}

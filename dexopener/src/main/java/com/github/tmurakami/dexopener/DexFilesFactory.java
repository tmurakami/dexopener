package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

final class DexFilesFactory {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    private static final int MAX_SIZE_PER_CLASSES = 150;
    private static final DexFiles NULL_FILES = new DexFiles() {
        @Override
        public DexFile get(String className) throws IOException {
            return null;
        }
    };

    private final ClassNameFilter classNameFilter;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFilesFactory(ClassNameFilter classNameFilter, File cacheDir, DexFileLoader dexFileLoader) {
        this.classNameFilter = classNameFilter;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    DexFiles newDexFiles(byte[] bytecode) {
        DexBackedDexFile file = new DexBackedDexFile(OPCODES, bytecode);
        Set<Set<ClassDef>> classesSet = getClassesSetToBeOpened(file);
        return classesSet.isEmpty() ? NULL_FILES : new DexFilesImpl(OPCODES,
                                                                    new HashMap<String, DexFile>(),
                                                                    classesSet,
                                                                    cacheDir,
                                                                    dexFileLoader);
    }

    private Set<Set<ClassDef>> getClassesSetToBeOpened(DexBackedDexFile file) {
        Set<Set<ClassDef>> classesSet = new HashSet<>();
        Set<ClassDef> classes = new HashSet<>();
        for (ClassDef def : file.getClasses()) {
            if (classNameFilter.accept(TypeUtils.getClassName(def.getType()))) {
                classes.add(ImmutableClassDef.of(def));
                if (classes.size() == MAX_SIZE_PER_CLASSES) {
                    classesSet.add(Collections.unmodifiableSet(classes));
                    classes = new HashSet<>();
                }
            }
        }
        if (!classes.isEmpty()) {
            classesSet.add(Collections.unmodifiableSet(classes));
        }
        return classesSet;
    }

}

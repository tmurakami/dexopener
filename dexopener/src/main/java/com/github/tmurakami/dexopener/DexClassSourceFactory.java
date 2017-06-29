package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;
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

final class DexClassSourceFactory {

    // This was empirically determined. If this value is too large, dex file generation will be
    // slow.
    private static final int MAX_CLASSES_PER_DEX_FILE = 150;

    private static final Opcodes OPCODES = Opcodes.getDefault();
    private static final ClassSource NULL_SOURCE = new ClassSource() {
        @Override
        public ClassFile getClassFile(String s) throws IOException {
            return null;
        }
    };

    private final ClassNameFilter classNameFilter;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexClassSourceFactory(ClassNameFilter classNameFilter,
                          File cacheDir,
                          DexFileLoader dexFileLoader,
                          DexClassFileFactory dexClassFileFactory) {
        this.classNameFilter = classNameFilter;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    @SuppressWarnings("deprecation")
    ClassSource newClassSource(byte[] bytecode) {
        DexBackedDexFile file = new DexBackedDexFile(OPCODES, bytecode);
        Set<Set<ClassDef>> classesSet = getClassesSetToBeOpened(file);
        if (classesSet.isEmpty()) {
            return NULL_SOURCE;
        } else {
            return new DexClassSource(OPCODES,
                                      new HashMap<String, dalvik.system.DexFile>(),
                                      classesSet,
                                      cacheDir,
                                      dexFileLoader,
                                      dexClassFileFactory);
        }
    }

    private Set<Set<ClassDef>> getClassesSetToBeOpened(DexBackedDexFile file) {
        Set<Set<ClassDef>> classesSet = new HashSet<>();
        Set<ClassDef> classes = new HashSet<>();
        for (ClassDef def : file.getClasses()) {
            if (classNameFilter.accept(TypeUtils.getClassName(def.getType()))) {
                // Since the `def` has bytecode to eat many memory, we create a copy for it to
                // release its bytecode reference.
                classes.add(ImmutableClassDef.of(def));
                // Generating a dex file for multiple classes at once is faster than processing per
                // class. So we create a set of `ClassDef`s using the empirically determined value.
                if (classes.size() == MAX_CLASSES_PER_DEX_FILE) {
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

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileHolderMapper {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    // Empirically determined value. Increasing this will slow DEX file generation.
    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final ClassNameFilter classNameFilter;
    private final DexFileGenerator dexFileGenerator;

    DexFileHolderMapper(ClassNameFilter classNameFilter, DexFileGenerator dexFileGenerator) {
        this.classNameFilter = classNameFilter;
        this.dexFileGenerator = dexFileGenerator;
    }

    void map(byte[] bytecode, Map<String, DexFileHolder> holderMap) {
        Set<ClassDef> classesToBeOpened = new HashSet<>();
        DexFileHolderImpl holder = new DexFileHolderImpl();
        for (ClassDef def : new DexBackedDexFile(OPCODES, bytecode).getClasses()) {
            String className = dexToJavaName(def.getType());
            if (classNameFilter.accept(className)) {
                Logger logger = Loggers.get();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Class to be opened: " + className);
                }
                classesToBeOpened.add(def);
                holderMap.put(className, holder);
                // It is faster to generate a DEX file for multiple classes at once than for one
                // class.
                if (classesToBeOpened.size() == MAX_CLASSES_PER_DEX_FILE) {
                    holder.setDexFileTask(generateDexFile(classesToBeOpened));
                    classesToBeOpened = new HashSet<>();
                    holder = new DexFileHolderImpl();
                }
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            holder.setDexFileTask(generateDexFile(classesToBeOpened));
        }
    }

    @SuppressWarnings("deprecation")
    private FutureTask<dalvik.system.DexFile> generateDexFile(Set<ClassDef> classesToBeOpened) {
        return dexFileGenerator.generateDexFile(new ImmutableDexFile(OPCODES, classesToBeOpened));
    }

    private static String dexToJavaName(String dexName) {
        // The `dexName` should be neither a primitive type nor an array type.
        return dexName.substring(1, dexName.length() - 1).replace('/', '.');
    }

}

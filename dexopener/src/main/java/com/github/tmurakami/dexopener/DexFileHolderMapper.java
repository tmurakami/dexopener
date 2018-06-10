package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileHolderMapper {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    // Empirically determined value. Increasing this will slow DEX file generation.
    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final ClassNameFilter classNameFilter;
    private final Executor executor;
    private final DexFileTaskFactory dexFileTaskFactory;

    DexFileHolderMapper(ClassNameFilter classNameFilter,
                        Executor executor,
                        DexFileTaskFactory dexFileTaskFactory) {
        this.classNameFilter = classNameFilter;
        this.executor = executor;
        this.dexFileTaskFactory = dexFileTaskFactory;
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
                    holder.setDexFileTask(newDexFileTask(classesToBeOpened));
                    classesToBeOpened = new HashSet<>();
                    holder = new DexFileHolderImpl();
                }
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            holder.setDexFileTask(newDexFileTask(classesToBeOpened));
        }
    }

    @SuppressWarnings("deprecation")
    private FutureTask<dalvik.system.DexFile> newDexFileTask(Set<ClassDef> classesToBeOpened) {
        DexFile dexFile = new ImmutableDexFile(OPCODES, classesToBeOpened);
        FutureTask<dalvik.system.DexFile> dexFileTask = dexFileTaskFactory.newDexFileTask(dexFile);
        // Run the task in the background in order to improve performance.
        executor.execute(dexFileTask);
        return dexFileTask;
    }

    private static String dexToJavaName(String dexName) {
        // The `dexName` must be neither a primitive type nor an array type.
        return dexName.substring(1, dexName.length() - 1).replace('/', '.');
    }

}

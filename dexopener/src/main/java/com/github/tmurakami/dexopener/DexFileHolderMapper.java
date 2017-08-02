package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
final class DexFileHolderMapper {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    // This was empirically determined. If this value is too large, dex file generation will be
    // slow.
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
            String type = def.getType();
            String className = type.substring(1, type.length() - 1).replace('/', '.');
            if (classNameFilter.accept(className)) {
                Logger logger = Loggers.get();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Class to be opened: " + className);
                }
                classesToBeOpened.add(def);
                holderMap.put(className, holder);
                // Generating a dex file for multiple classes at once is faster than processing per
                // class.
                if (classesToBeOpened.size() == MAX_CLASSES_PER_DEX_FILE) {
                    holder.setTask(newDexFileTask(classesToBeOpened));
                    classesToBeOpened = new HashSet<>();
                    holder = new DexFileHolderImpl();
                }
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            holder.setTask(newDexFileTask(classesToBeOpened));
        }
    }

    private FutureTask<dalvik.system.DexFile> newDexFileTask(Set<ClassDef> classesToBeOpened) {
        FutureTask<dalvik.system.DexFile> task =
                dexFileTaskFactory.newDexFileTask(new ImmutableDexFile(OPCODES, classesToBeOpened));
        // In order to improve performance when opening many classes, run the task in the
        // background.
        executor.execute(task);
        return task;
    }

}

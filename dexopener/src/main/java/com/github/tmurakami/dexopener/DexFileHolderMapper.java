package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import dalvik.system.DexFile;

@SuppressWarnings("deprecation")
final class DexFileHolderMapper {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    // This was empirically determined. If this value is too large, dex file generation will be
    // slow.
    private static final int MAX_CLASSES_PER_DEX_FILE = 150;

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
            String className = TypeUtils.getClassName(def.getType());
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
                    holder.task = newDexFileTask(classesToBeOpened);
                    classesToBeOpened = new HashSet<>();
                    holder = new DexFileHolderImpl();
                }
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            holder.task = newDexFileTask(classesToBeOpened);
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

    private static class DexFileHolderImpl implements DexFileHolder {

        FutureTask<dalvik.system.DexFile> task;

        @Override
        public DexFile get() throws IOException {
            // Since the task might not be finished to do, we do it here.
            task.run();
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        return task.get();
                    } catch (InterruptedException e) {
                        // Refuse to be interrupted
                        interrupted = true;
                    }
                }
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new IllegalStateException("Unexpected error", e);
                }
            } finally {
                if (interrupted) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

}

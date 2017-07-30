package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileTask implements Callable<dalvik.system.DexFile> {

    private final Opcodes opcodes;
    private Set<ClassDef> classesToBeOpened;
    private final File cacheDir;
    private final ClassOpener classOpener;
    private final DexFileLoader dexFileLoader;

    DexFileTask(Opcodes opcodes,
                Set<ClassDef> classesToBeOpened,
                File cacheDir,
                ClassOpener classOpener,
                DexFileLoader dexFileLoader) {
        this.opcodes = opcodes;
        this.classesToBeOpened = classesToBeOpened;
        this.cacheDir = cacheDir;
        this.classOpener = classOpener;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public dalvik.system.DexFile call() throws IOException {
        DexPool pool = newDexPool();
        File cacheDir = this.cacheDir;
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IOException("Cannot create " + cacheDir);
        }
        File dex = File.createTempFile("classes", ".dex", cacheDir);
        try {
            pool.writeTo(new FileDataStore(dex));
            String sourcePathName = dex.getCanonicalPath();
            String outputPathName = sourcePathName + ".opt";
            dalvik.system.DexFile dexFile =
                    dexFileLoader.loadDex(sourcePathName, outputPathName, 0);
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("DEX file generated: " + outputPathName);
            }
            return dexFile;
        } finally {
            FileUtils.delete(dex);
        }
    }

    private DexPool newDexPool() {
        try {
            DexPool pool = new DexPool(opcodes);
            ClassOpener classOpener = this.classOpener;
            for (ClassDef def : classesToBeOpened) {
                pool.internClass(classOpener.openClass(def));
            }
            return pool;
        } finally {
            // The `classesToBeOpened` might have the bytecode that eat many memory, so we release
            // it here.
            classesToBeOpened = null;
        }
    }

}

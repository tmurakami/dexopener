package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;

import java.io.File;
import java.util.Set;
import java.util.concurrent.FutureTask;

final class DexFileTaskFactory {

    private final File cacheDir;
    private final ClassOpener classOpener;
    private final DexFileLoader dexFileLoader;

    DexFileTaskFactory(File cacheDir, ClassOpener classOpener, DexFileLoader dexFileLoader) {
        this.cacheDir = cacheDir;
        this.classOpener = classOpener;
        this.dexFileLoader = dexFileLoader;
    }

    FutureTask<dalvik.system.DexFile> newDexFileTask(Opcodes opcodes,
                                                     Set<ClassDef> classesToBeOpened) {
        return new FutureTask<>(new DexFileTask(opcodes,
                                                classesToBeOpened,
                                                cacheDir,
                                                classOpener,
                                                dexFileLoader));
    }

}

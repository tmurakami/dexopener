package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.util.concurrent.FutureTask;

final class DexFileTaskFactory {

    private final File cacheDir;

    DexFileTaskFactory(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @SuppressWarnings("deprecation")
    FutureTask<dalvik.system.DexFile> newDexFileTask(DexFile dexFile) {
        return new FutureTask<>(new OpenDexFile(dexFile, cacheDir));
    }

}

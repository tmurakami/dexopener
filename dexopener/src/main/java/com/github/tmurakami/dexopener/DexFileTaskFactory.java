package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;

import java.io.File;
import java.util.concurrent.FutureTask;

final class DexFileTaskFactory {

    private final File cacheDir;
    private final DexRewriter dexRewriter = new DexRewriter(new FinalModifierRemoverModule());
    private final DexFileLoader dexFileLoader = new DexFileLoader();

    DexFileTaskFactory(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @SuppressWarnings("deprecation")
    FutureTask<dalvik.system.DexFile> newDexFileTask(DexFile dexFile) {
        return new FutureTask<>(new OpenDexFile(dexFile, dexRewriter, cacheDir, dexFileLoader));
    }

}

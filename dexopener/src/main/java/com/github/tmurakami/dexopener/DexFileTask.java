package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileTask implements Callable<dalvik.system.DexFile> {

    private DexFile dexFile;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFileTask(DexFile dexFile, File cacheDir, DexFileLoader dexFileLoader) {
        this.dexFile = dexFile;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public dalvik.system.DexFile call() throws IOException {
        File cacheDir = this.cacheDir;
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IOException("Cannot create " + cacheDir);
        }
        DexFile opened = new DexRewriter(new DexOpenerRewriterModule()).rewriteDexFile(dexFile);
        File dex = File.createTempFile("classes", ".dex", cacheDir);
        try {
            DexPool.writeTo(new FileDataStore(dex), opened);
            String sourcePathName = dex.getCanonicalPath();
            String outputPathName = sourcePathName + ".opt";
            dalvik.system.DexFile file =
                    dexFileLoader.loadDex(sourcePathName, outputPathName, 0);
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("DEX file generated: " + outputPathName);
            }
            return file;
        } finally {
            // The `dexFile` might have the bytecode that eat many memory, so we release it here.
            dexFile = null;
            FileUtils.delete(dex);
        }
    }

}

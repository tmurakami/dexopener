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

@SuppressWarnings("deprecation")
final class DexFileTask implements Callable<dalvik.system.DexFile> {

    private DexFile dexFile;
    private final DexRewriter dexRewriter;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFileTask(DexFile dexFile,
                DexRewriter dexRewriter,
                File cacheDir,
                DexFileLoader dexFileLoader) {
        this.dexFile = dexFile;
        this.dexRewriter = dexRewriter;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public dalvik.system.DexFile call() throws IOException {
        try {
            return generateDex(dexRewriter.rewriteDexFile(dexFile));
        } finally {
            // The `dexFile` has the bytecode that may eat many memory, so we release it here.
            dexFile = null;
        }
    }

    private dalvik.system.DexFile generateDex(DexFile dexFile) throws IOException {
        File cacheDir = this.cacheDir;
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IOException("Cannot create " + cacheDir);
        }
        File dex = File.createTempFile("classes", ".dex", cacheDir);
        // The extension of the source file must be `dex`.
        File tmp = new File(cacheDir, dex.getName() + ".tmp.dex");
        String dexPath = dex.getCanonicalPath();
        dalvik.system.DexFile file;
        try {
            DexPool.writeTo(new FileDataStore(tmp), dexFile);
            file = dexFileLoader.loadDex(tmp.getCanonicalPath(), dexPath, 0);
        } finally {
            FileUtils.delete(tmp);
        }
        Logger logger = Loggers.get();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("DEX file generated: " + dexPath);
        }
        return file;
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileOpener {

    private final Executor executor;
    private final File cacheDir;

    DexFileOpener(Executor executor, File cacheDir) {
        this.executor = executor;
        this.cacheDir = cacheDir;
    }

    @SuppressWarnings("deprecation")
    RunnableFuture<dalvik.system.DexFile> openDexFile(DexFile dexFile) {
        OpenDexFile openDexFile = new OpenDexFile(dexFile, cacheDir);
        RunnableFuture<dalvik.system.DexFile> future = new FutureTask<>(openDexFile);
        // Run the future in the background in order to improve performance.
        executor.execute(future);
        return future;
    }

    @SuppressWarnings("deprecation")
    private static class OpenDexFile implements Callable<dalvik.system.DexFile> {

        private DexFile dexFile;
        private final File cacheDir;

        OpenDexFile(DexFile dexFile, File cacheDir) {
            this.dexFile = dexFile;
            this.cacheDir = cacheDir;
        }

        @Override
        public dalvik.system.DexFile call() throws IOException {
            DexRewriter dexRewriter = new DexRewriter(new FinalModifierRemoverModule());
            try {
                return generateDexFile(dexRewriter.rewriteDexFile(dexFile), cacheDir);
            } finally {
                // The `dexFile` may have bytecode to eat a lot of memory, so we release it here.
                dexFile = null;
            }
        }

        private static dalvik.system.DexFile generateDexFile(DexFile dexFile, File cacheDir)
                throws IOException {
            if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
                throw new IOException("Cannot create " + cacheDir);
            }
            File dex = File.createTempFile("classes", ".dex", cacheDir);
            dex.deleteOnExit();
            String dexPath = dex.getCanonicalPath();
            // The extension of the source file must be `dex`.
            File tmp = new File(cacheDir, dex.getName() + ".tmp.dex");
            String tmpPath = tmp.getCanonicalPath();
            dalvik.system.DexFile file;
            try {
                DexPool.writeTo(new FileDataStore(tmp), dexFile);
                file = dalvik.system.DexFile.loadDex(tmpPath, dexPath, 0);
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

}

/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ClassOpener {

    private final Executor executor;
    private final File cacheDir;

    ClassOpener(Executor executor, File cacheDir) {
        this.executor = executor;
        this.cacheDir = cacheDir;
    }

    @SuppressWarnings("deprecation")
    RunnableFuture<? extends dalvik.system.DexFile> openClasses(Opcodes opcodes,
                                                                Set<? extends ClassDef> classes) {
        OpenDexFile openDexFile = new OpenDexFile(opcodes, classes, cacheDir);
        RunnableFuture<dalvik.system.DexFile> future = new FutureTask<>(openDexFile);
        // Run the future in the background in order to improve performance.
        executor.execute(future);
        return future;
    }

    @SuppressWarnings("deprecation")
    private static class OpenDexFile implements Callable<dalvik.system.DexFile> {

        private final Opcodes opcodes;
        private Set<? extends ClassDef> classes;
        private final File cacheDir;

        OpenDexFile(Opcodes opcodes, Set<? extends ClassDef> classes, File cacheDir) {
            this.opcodes = opcodes;
            this.classes = classes;
            this.cacheDir = cacheDir;
        }

        @Override
        public dalvik.system.DexFile call() throws IOException {
            DexFile dexFile = new ImmutableDexFile(opcodes, classes);
            DexRewriter dexRewriter = new DexRewriter(new FinalModifierRemoverModule());
            try {
                return generateDexFile(dexRewriter.rewriteDexFile(dexFile), cacheDir);
            } finally {
                // The `classes` may have bytecode to eat a lot of memory, so we release it here.
                classes = null;
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

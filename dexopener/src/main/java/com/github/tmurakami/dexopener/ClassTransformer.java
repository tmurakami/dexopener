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
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
final class ClassTransformer implements Callable<dalvik.system.DexFile>, DexFile {

    private final Opcodes opcodes;
    private final File codeCacheDir;
    private final DexFileLoader dexFileLoader;
    private Set<? extends ClassDef> classes;

    ClassTransformer(Opcodes opcodes,
                     Set<? extends ClassDef> classes,
                     File codeCacheDir,
                     DexFileLoader dexFileLoader) {
        this.opcodes = opcodes;
        this.classes = classes;
        this.codeCacheDir = codeCacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public Opcodes getOpcodes() {
        return opcodes;
    }

    @Override
    public Set<? extends ClassDef> getClasses() {
        return classes;
    }

    @Override
    public dalvik.system.DexFile call() throws IOException {
        DexRewriter dexRewriter = new DexRewriter(new FinalModifierRemoverModule());
        try {
            DexFile dexFile = dexRewriter.getDexFileRewriter().rewrite(this);
            File dex = File.createTempFile("classes", ".dex", codeCacheDir);
            dex.deleteOnExit();
            String dexPath = dex.getCanonicalPath();
            // The extension of the source file must be `dex`.
            File tmp = new File(codeCacheDir, dex.getName() + ".tmp.dex");
            String tmpPath = tmp.getCanonicalPath();
            dalvik.system.DexFile file;
            try {
                DexPool.writeTo(new FileDataStore(tmp), dexFile);
                file = dexFileLoader.loadDex(tmpPath, dexPath);
            } finally {
                FileUtils.delete(tmp);
            }
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Generated " + dexPath);
            }
            return file;
        } finally {
            // The `classes` may hold bytecode that eats a lot of memory, so we release it here.
            classes = Collections.emptySet();
        }
    }

}

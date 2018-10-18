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
final class ClassTransformationTask implements Callable<dalvik.system.DexFile>, DexFile {

    private final Opcodes opcodes;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private Set<? extends ClassDef> classes = Collections.emptySet();

    ClassTransformationTask(Opcodes opcodes, File cacheDir, DexFileLoader dexFileLoader) {
        this.opcodes = opcodes;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    void setClasses(Set<? extends ClassDef> classes) {
        this.classes = classes;
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
            DexFile dexFile = dexRewriter.rewriteDexFile(this);
            File dex = File.createTempFile("classes", ".dex", cacheDir);
            dex.deleteOnExit();
            String dexPath = dex.getCanonicalPath();
            // The extension of the source file must be `dex`.
            File tmp = new File(cacheDir, dex.getName() + ".tmp.dex");
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
                logger.finest("DEX file generated: " + dexPath);
            }
            return file;
        } finally {
            // The `classes` has bytecode to eat a lot of memory, so we release it here.
            classes = Collections.emptySet();
        }
    }

}

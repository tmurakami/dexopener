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

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileGenerator {

    private final File cacheDir;

    DexFileGenerator(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @SuppressWarnings("deprecation")
    dalvik.system.DexFile generateDexFile(DexFile dexFile) throws IOException {
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

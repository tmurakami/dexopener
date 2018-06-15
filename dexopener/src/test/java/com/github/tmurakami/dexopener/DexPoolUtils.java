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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

final class DexPoolUtils {

    private DexPoolUtils() {
        throw new AssertionError("Do not instantiate");
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    static byte[] toBytecode(DexFile dexFile) throws IOException {
        File tmp = File.createTempFile("classes", ".dex");
        DexPool.writeTo(new FileDataStore(tmp), dexFile);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmp));
        try {
            return IOUtils.readBytes(in);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
            FileUtils.delete(tmp);
        }
    }

}

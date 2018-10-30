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

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class DexBackedDexFileUtils {

    private DexBackedDexFileUtils() {
        throw new AssertionError("Do not instantiate");
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    static DexBackedDexFile loadDexFile(String path) throws IOException {
        InputStream in = new FileInputStream(path);
        try {
            return new DexBackedDexFile(null, IOUtils.readBytes(in));
        } finally {
            in.close();
        }
    }

}

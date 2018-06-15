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

import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class AndroidClassSourceFactory {

    private static final Executor EXECUTOR;

    static {
        final AtomicInteger count = new AtomicInteger();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int nThreads = Math.max(1, Math.min(availableProcessors, 4)); // 1 to 4
        EXECUTOR = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "DexOpener #" + count.incrementAndGet());
            }
        });
    }

    private final ClassNameFilter classNameFilter;

    AndroidClassSourceFactory(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    ClassSource newClassSource(String sourceDir, File cacheDir) {
        return new AndroidClassSource(sourceDir,
                                      classNameFilter,
                                      newDexFileHolderMapper(cacheDir),
                                      new DexClassSourceFactory());
    }

    private DexFileHolderMapper newDexFileHolderMapper(File cacheDir) {
        return new DexFileHolderMapper(classNameFilter, new DexFileOpener(EXECUTOR, cacheDir));
    }

}

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

import android.content.Context;
import android.os.Build;

import com.github.tmurakami.dexopener.repackaged.com.google.common.base.Function;
import com.github.tmurakami.dexopener.repackaged.com.google.common.base.Predicate;
import com.github.tmurakami.dexopener.repackaged.com.google.common.io.ByteStreams;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.zip.ZipFile;

import static com.github.tmurakami.dexopener.repackaged.com.google.common.base.Functions.constant;
import static com.github.tmurakami.dexopener.repackaged.com.google.common.base.Predicates.compose;
import static com.github.tmurakami.dexopener.repackaged.com.google.common.collect.Iterables.filter;
import static com.github.tmurakami.dexopener.repackaged.com.google.common.collect.Iterables.partition;
import static com.github.tmurakami.dexopener.repackaged.com.google.common.collect.Iterables.transform;
import static com.github.tmurakami.dexopener.repackaged.com.google.common.collect.Maps.toMap;
import static com.github.tmurakami.dexopener.repackaged.com.google.common.collect.Maps.transformValues;
import static java.util.Collections.list;

@SuppressWarnings("deprecation")
class ClassPath {

    // Empirically determined value. Increasing this will slow DEX file generation.
    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final Context context;
    private final Predicate<? super String> dexNameFilter;
    private final DexFileLoader dexFileLoader;
    private final Executor executor;
    private Map<String, dalvik.system.DexFile> dexFileMap;

    ClassPath(Context context,
              Predicate<? super String> dexNameFilter,
              DexFileLoader dexFileLoader,
              Executor executor) {
        this.context = context;
        this.dexNameFilter = dexNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.executor = executor;
    }

    Class loadClass(String className, ClassLoader loader) {
        dalvik.system.DexFile dexFile = getDexFileFor(className);
        return dexFile == null ? null : dexFile.loadClass(className, loader);
    }

    private dalvik.system.DexFile getDexFileFor(String className) {
        String dexName = ReflectionUtils.javaToDexName(className);
        if (!dexNameFilter.apply(dexName)) {
            return null;
        }
        Map<String, dalvik.system.DexFile> map = dexFileMap;
        if (map == null) {
            dexFileMap = map = collectDexFiles();
        }
        return map.get(dexName);
    }

    private Map<String, dalvik.system.DexFile> collectDexFiles() {
        File codeCacheDir = getCodeCacheDir(context);
        Predicate<ClassDef> classDefFilter = compose(dexNameFilter, ClassDef::getType);
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        String sourceDir = context.getApplicationInfo().sourceDir;
        try (ZipFile zipFile = new ZipFile(sourceDir)) {
            for (DexFile dexFile : dexFiles(zipFile)) {
                Opcodes opcodes = dexFile.getOpcodes();
                Iterable<? extends ClassDef> classes = filter(dexFile.getClasses(), classDefFilter);
                for (List<? extends ClassDef> list : partition(classes, MAX_CLASSES_PER_DEX_FILE)) {
                    Set<ClassDef> set = new HashSet<>(list);
                    ClassTransformer transformer =
                            new ClassTransformer(opcodes, set, codeCacheDir, dexFileLoader);
                    RunnableFuture<dalvik.system.DexFile> future = new FutureTask<>(transformer);
                    executor.execute(future);
                    futureMap.putAll(toMap(transform(set, ClassDef::getType), constant(future)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return transformValues(futureMap, runnableFutureResult());
    }

    private static File getCodeCacheDir(Context context) {
        File parentDir;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            parentDir = new File(context.getApplicationInfo().dataDir, "code_cache");
        } else {
            parentDir = context.getCodeCacheDir();
        }
        File cacheDir = new File(parentDir, "dexopener");
        if (cacheDir.isDirectory() || cacheDir.mkdirs()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        return cacheDir;
    }

    private static Iterable<DexFile> dexFiles(ZipFile zipFile) {
        return transform(filter(list(zipFile.entries()), entry -> {
            String name = entry.getName();
            return name.startsWith("classes") && name.endsWith(".dex");
        }), entry -> {
            try (InputStream in = zipFile.getInputStream(entry)) {
                return new DexBackedDexFile(null, ByteStreams.toByteArray(in));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static <T> Function<RunnableFuture<T>, T> runnableFutureResult() {
        return future -> {
            // The future might not be completed, so we do it here first.
            future.run();
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        // Refuse to be interrupted
                        interrupted = true;
                    }
                }
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            } finally {
                if (interrupted) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

}

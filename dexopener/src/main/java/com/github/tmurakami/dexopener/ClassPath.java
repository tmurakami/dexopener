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
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.IOError;
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
    private final Predicate<? super String> classNameFilter;
    private final DexFileLoader dexFileLoader;
    private final Executor executor;
    private Map<String, dalvik.system.DexFile> dexFileMap;

    ClassPath(Context context,
              Predicate<? super String> classNameFilter,
              DexFileLoader dexFileLoader,
              Executor executor) {
        this.context = context;
        this.classNameFilter = classNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.executor = executor;
    }

    Class loadClass(String className, ClassLoader loader) {
        if (classNameFilter.apply(className)) {
            dalvik.system.DexFile dexFile = getDexFileMap().get(className);
            if (dexFile != null) {
                return dexFile.loadClass(className, loader);
            }
        }
        return null;
    }

    private Map<String, dalvik.system.DexFile> getDexFileMap() {
        if (dexFileMap != null) {
            return dexFileMap;
        }
        try {
            return dexFileMap = collectDexFiles();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private Map<String, dalvik.system.DexFile> collectDexFiles() throws IOException {
        File codeCacheDir = getCodeCacheDir(context);
        Function<ClassDef, String> classDefToClassName = classDefToJavaName();
        Predicate<ClassDef> classFilter = compose(classNameFilter, classDefToClassName);
        Map<String, FutureTask<dalvik.system.DexFile>> taskMap = new HashMap<>();
        String sourceDir = context.getApplicationInfo().sourceDir;
        try (ZipFile zipFile = new ZipFile(sourceDir)) {
            for (DexFile dexFile : dexFiles(zipFile)) {
                Opcodes opcodes = dexFile.getOpcodes();
                Iterable<? extends ClassDef> classes = filter(dexFile.getClasses(), classFilter);
                for (List<? extends ClassDef> list : partition(classes, MAX_CLASSES_PER_DEX_FILE)) {
                    Set<ClassDef> set = new HashSet<>(list);
                    ClassTransformationTask task =
                            new ClassTransformationTask(opcodes, set, codeCacheDir, dexFileLoader);
                    FutureTask<dalvik.system.DexFile> future = new FutureTask<>(task);
                    executor.execute(future);
                    taskMap.putAll(toMap(transform(set, classDefToClassName), constant(future)));
                }
            }
        }
        return transformValues(taskMap, dexFileFutureTaskToDexFile());
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
                throw new IOError(e);
            }
        });
    }

    private static Function<ClassDef, String> classDefToJavaName() {
        return classDef -> {
            String type = classDef.getType();
            // `type` should be neither a primitive type nor an array type.
            return type.substring(1, type.length() - 1).replace('/', '.');
        };
    }

    private static Function<FutureTask<dalvik.system.DexFile>, dalvik.system.DexFile> dexFileFutureTaskToDexFile() {
        return task -> {
            // The task might not be completed, so we do it here first.
            task.run();
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        return task.get();
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
                    throw new IllegalStateException(cause);
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

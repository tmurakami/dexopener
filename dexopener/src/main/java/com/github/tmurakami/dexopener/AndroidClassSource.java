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

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("deprecation")
final class AndroidClassSource implements ClassSource {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    // Empirically determined value. Increasing this will slow DEX file generation.
    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final Executor executor;
    private ClassSource delegate;

    AndroidClassSource(String sourceDir,
                       ClassNameFilter classNameFilter,
                       File cacheDir,
                       DexFileLoader dexFileLoader,
                       Executor executor) {
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.executor = executor;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        return classNameFilter.accept(className) ? getDelegate().getClassFile(className) : null;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private ClassSource getDelegate() throws IOException {
        ClassSource source = delegate;
        if (source != null) {
            return source;
        }
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            map(in, futureMap);
        } finally {
            in.close();
        }
        if (futureMap.isEmpty()) {
            throw new IllegalStateException("There are no classes to be opened");
        }
        return delegate = new DexClassSource(futureMap);
    }

    private void map(ZipInputStream in,
                     Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap)
            throws IOException {
        Logger logger = Loggers.get();
        Set<ClassDef> classesToBeOpened = new HashSet<>();
        ClassTransformationTask task = new ClassTransformationTask(OPCODES, cacheDir, dexFileLoader);
        RunnableFuture<dalvik.system.DexFile> future = new FutureTask<>(task);
        for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
            String name = e.getName();
            if (!name.startsWith("classes") || !name.endsWith(".dex")) {
                continue;
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Reading the entry " + name + " from " + sourceDir);
            }
            DexFile dexFile = new DexBackedDexFile(OPCODES, IOUtils.readBytes(in));
            for (ClassDef def : dexFile.getClasses()) {
                String dexName = def.getType();
                // `dexName` should be neither a primitive type nor an array type.
                String className = dexName.substring(1, dexName.length() - 1).replace('/', '.');
                if (!classNameFilter.accept(className)) {
                    continue;
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Class to be opened: " + className);
                }
                classesToBeOpened.add(def);
                futureMap.put(className, future);
                // It is faster to generate a DEX file for multiple classes at once than for a
                // single class.
                if (classesToBeOpened.size() < MAX_CLASSES_PER_DEX_FILE) {
                    continue;
                }
                task.setClasses(classesToBeOpened);
                executor.execute(future);
                classesToBeOpened = new HashSet<>();
                task = new ClassTransformationTask(OPCODES, cacheDir, dexFileLoader);
                future = new FutureTask<>(task);
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            task.setClasses(classesToBeOpened);
            executor.execute(future);
        }
    }

}

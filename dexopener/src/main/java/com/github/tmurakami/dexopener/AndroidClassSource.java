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

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class AndroidClassSource implements ClassSource {

    // Empirically determined value. Increasing this will slow DEX file generation.
    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final Opcodes opcodes;
    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private final ClassOpener classOpener;
    private ClassSource delegate;

    AndroidClassSource(Opcodes opcodes,
                       String sourceDir,
                       ClassNameFilter classNameFilter,
                       ClassOpener classOpener) {
        this.opcodes = opcodes;
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
        this.classOpener = classOpener;
    }

    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        return classNameFilter.accept(className) ? getDelegate().getClassFile(className) : null;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private ClassSource getDelegate() throws IOException {
        ClassSource source = delegate;
        if (source != null) {
            return source;
        }
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            map(in, holderMap);
        } finally {
            in.close();
        }
        if (holderMap.isEmpty()) {
            throw new IllegalStateException("There are no classes to be opened");
        }
        return delegate = new DexClassSource(holderMap);
    }

    private void map(ZipInputStream in, Map<String, DexFileHolder> holderMap) throws IOException {
        Logger logger = Loggers.get();
        Set<ClassDef> classesToBeOpened = new HashSet<>();
        DexFileHolderImpl holder = new DexFileHolderImpl();
        for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
            String name = e.getName();
            if (!name.startsWith("classes") || !name.endsWith(".dex")) {
                continue;
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Reading the entry " + name + " from " + sourceDir);
            }
            DexFile dexFile = new DexBackedDexFile(opcodes, IOUtils.readBytes(in));
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
                holderMap.put(className, holder);
                // It is faster to generate a DEX file for multiple classes at once than for a
                // single class.
                if (classesToBeOpened.size() < MAX_CLASSES_PER_DEX_FILE) {
                    continue;
                }
                holder.setDexFileFuture(openClasses(classesToBeOpened));
                classesToBeOpened = new HashSet<>();
                holder = new DexFileHolderImpl();
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            holder.setDexFileFuture(openClasses(classesToBeOpened));
        }
    }

    @SuppressWarnings("deprecation")
    private RunnableFuture<? extends dalvik.system.DexFile> openClasses(Set<? extends ClassDef> classes) {
        return classOpener.openClasses(opcodes, classes);
    }

}

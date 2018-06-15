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
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DexFileHolderMapper {

    private static final Opcodes OPCODES = Opcodes.getDefault();
    // Empirically determined value. Increasing this will slow DEX file generation.
    private static final int MAX_CLASSES_PER_DEX_FILE = 100;

    private final ClassNameFilter classNameFilter;
    private final DexFileOpener dexFileOpener;

    DexFileHolderMapper(ClassNameFilter classNameFilter, DexFileOpener dexFileOpener) {
        this.classNameFilter = classNameFilter;
        this.dexFileOpener = dexFileOpener;
    }

    void map(byte[] bytecode, Map<String, DexFileHolder> holderMap) {
        Set<ClassDef> classesToBeOpened = new HashSet<>();
        DexFileHolderImpl holder = new DexFileHolderImpl();
        for (ClassDef def : new DexBackedDexFile(OPCODES, bytecode).getClasses()) {
            String className = dexToJavaName(def.getType());
            if (classNameFilter.accept(className)) {
                Logger logger = Loggers.get();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Class to be opened: " + className);
                }
                classesToBeOpened.add(def);
                holderMap.put(className, holder);
                // It is faster to generate a DEX file for multiple classes at once than for one
                // class.
                if (classesToBeOpened.size() == MAX_CLASSES_PER_DEX_FILE) {
                    holder.setDexFileFuture(openClasses(classesToBeOpened));
                    classesToBeOpened = new HashSet<>();
                    holder = new DexFileHolderImpl();
                }
            }
        }
        if (!classesToBeOpened.isEmpty()) {
            holder.setDexFileFuture(openClasses(classesToBeOpened));
        }
    }

    @SuppressWarnings("deprecation")
    private RunnableFuture<dalvik.system.DexFile> openClasses(Set<ClassDef> classesToBeOpened) {
        return dexFileOpener.openDexFile(new ImmutableDexFile(OPCODES, classesToBeOpened));
    }

    private static String dexToJavaName(String dexName) {
        // The `dexName` should be neither a primitive type nor an array type.
        return dexName.substring(1, dexName.length() - 1).replace('/', '.');
    }

}

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
import android.support.test.InstrumentationRegistry;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.AccessFlags;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RunnableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("deprecation")
public class ClassOpenerTest {

    @Rule
    public TemporaryFolder folder =
            new TemporaryFolder(InstrumentationRegistry.getTargetContext().getCacheDir());

    private final Executor executor = new Executor() {
        @Override
        public void execute(@NonNull Runnable runnable) {
            runnable.run();
        }
    };

    @Test
    public void future_should_generate_a_DexFile_of_which_final_modifiers_are_removed()
            throws Exception {
        ClassDef def = readFooBarClassDef();
        assertTrue(AccessFlags.FINAL.isSet(def.getAccessFlags()));
        ClassOpener classOpener = new ClassOpener(executor, folder.newFolder());
        RunnableFuture<? extends dalvik.system.DexFile> future =
                classOpener.openClasses(Opcodes.getDefault(), Collections.singleton(def));
        ClassLoader classLoader = new ClassLoader() {
        };
        Class<?> c = future.get().loadClass("foo.Bar", classLoader);
        assertSame(classLoader, c.getClassLoader());
        assertFalse(Modifier.isFinal(c.getModifiers()));
    }

    @Test(expected = IOException.class)
    public void getting_a_DexFile_should_cause_IOException_if_the_cache_directory_cannot_be_created()
            throws Throwable {
        ClassOpener classOpener = new ClassOpener(executor, folder.newFile());
        RunnableFuture<? extends dalvik.system.DexFile> future =
                classOpener.openClasses(Opcodes.getDefault(), Collections.<ClassDef>emptySet());
        try {
            future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static ClassDef readFooBarClassDef() throws IOException {
        String sourceDir = InstrumentationRegistry.getContext().getApplicationInfo().sourceDir;
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (!name.startsWith("classes") || !name.endsWith(".dex")) {
                    continue;
                }
                DexFile dexFile = new DexBackedDexFile(Opcodes.getDefault(), IOUtils.readBytes(in));
                for (ClassDef def : dexFile.getClasses()) {
                    if (def.getType().equals("Lfoo/Bar;")) {
                        return def;
                    }
                }
            }
        } finally {
            in.close();
        }
        throw new IllegalStateException("Cannot read foo.Bar");
    }

}

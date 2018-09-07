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

import android.support.test.InstrumentationRegistry;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertSame;

@SuppressWarnings("deprecation")
public class DexFileGeneratorTest {

    @Rule
    public TemporaryFolder folder =
            new TemporaryFolder(InstrumentationRegistry.getTargetContext().getCacheDir());

    @Test
    public void generateDexFile_should_generate_a_DexFile() throws IOException {
        DexFileGenerator dexFileGenerator = new DexFileGenerator(folder.newFolder());
        dalvik.system.DexFile dexFile = dexFileGenerator.generateDexFile(readTestDexFile());
        ClassLoader classLoader = new ClassLoader() {
        };
        assertSame(classLoader, dexFile.loadClass("foo.Bar", classLoader).getClassLoader());
    }

    @Test(expected = IOException.class)
    public void generateDexFile_should_throw_IOException_if_the_cache_directory_cannot_be_created()
            throws IOException {
        new DexFileGenerator(folder.newFile()).generateDexFile(readTestDexFile());
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static DexFile readTestDexFile() throws IOException {
        String sourceDir = InstrumentationRegistry.getContext().getApplicationInfo().sourceDir;
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    return new DexBackedDexFile(Opcodes.getDefault(), IOUtils.readBytes(in));
                }
            }
        } finally {
            in.close();
        }
        throw new IllegalStateException("Cannot read dex");
    }

}

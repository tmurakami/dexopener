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
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.google.common.io.ByteStreams;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClassTransformerTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock(stubOnly = true)
    private DexFileLoader dexFileLoader;
    @Mock(stubOnly = true)
    private dalvik.system.DexFile dexFile;

    @Captor
    private ArgumentCaptor<String> srcPathCaptor;

    @Test
    public void should_generate_non_final_classes() throws IOException {
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      AccessFlags.FINAL.getValue(),
                                                      "Ljava/lang/Object;",
                                                      null, null, null, null, null);
        byte[] bytes = DexPoolUtils.toBytecode(new ImmutableDexFile(Opcodes.getDefault(),
                                                                    Collections.singleton(def)));
        given(dexFileLoader.loadDex(srcPathCaptor.capture(), anyString()))
                .will(answer((String src, String out) -> {
                    assertTrue(src.endsWith("tmp.dex"));
                    assertTrue(out.endsWith(".dex"));
                    DexFile file;
                    try (InputStream in = new FileInputStream(src)) {
                        file = new DexBackedDexFile(null, ByteStreams.toByteArray(in));
                    }
                    Set<? extends ClassDef> classes = file.getClasses();
                    assertSame(1, classes.size());
                    assertFalse(AccessFlags.FINAL.isSet(classes.iterator().next().getAccessFlags()));
                    return dexFile;
                }));
        DexFile file = new DexBackedDexFile(null, bytes);
        ClassTransformer transformer = new ClassTransformer(file.getOpcodes(),
                                                            file.getClasses(),
                                                            folder.newFolder(),
                                                            dexFileLoader);
        assertSame(dexFile, transformer.call());
        assertTrue(transformer.getClasses().isEmpty());
        assertFalse(new File(srcPathCaptor.getValue()).exists());
    }

}

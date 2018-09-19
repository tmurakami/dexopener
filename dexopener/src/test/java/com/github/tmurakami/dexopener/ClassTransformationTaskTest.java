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

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.AccessFlags;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;

import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer2;

import java.io.File;
import java.io.IOException;
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
public class ClassTransformationTaskTest {

    private static final int ACCESS_FLAGS_FINAL = org.jf.dexlib2.AccessFlags.FINAL.getValue();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock(stubOnly = true)
    private DexFileLoader dexFileLoader;
    @Mock(stubOnly = true)
    private dalvik.system.DexFile dexFile;

    @Captor
    private ArgumentCaptor<String> srcPathCaptor;

    @Test
    public void call_should_generate_non_final_classes() throws IOException {
        ImmutableClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                                      ACCESS_FLAGS_FINAL,
                                                      "Ljava/lang/Object;",
                                                      null,
                                                      null,
                                                      Collections.<ImmutableAnnotation>emptySet(),
                                                      Collections.<ImmutableField>emptySet(),
                                                      Collections.<ImmutableMethod>emptySet());
        byte[] bytes = DexPoolUtils.toBytecode(new ImmutableDexFile(org.jf.dexlib2.Opcodes.getDefault(),
                                                                    Collections.singleton(def)));
        final Opcodes opcodes = Opcodes.getDefault();
        given(dexFileLoader.loadDex(srcPathCaptor.capture(), anyString()))
                .will(answer(new Answer2<dalvik.system.DexFile, String, String>() {
                    @SuppressWarnings("TryFinallyCanBeTryWithResources")
                    @Override
                    public dalvik.system.DexFile answer(String src, String out) throws Throwable {
                        assertTrue(src.endsWith("tmp.dex"));
                        assertTrue(out.endsWith(".dex"));
                        DexBackedDexFile file = DexBackedDexFileUtils.loadDexFile(opcodes, src);
                        Set<? extends ClassDef> classes = file.getClasses();
                        assertSame(1, classes.size());
                        assertFalse(AccessFlags.FINAL.isSet(classes.iterator().next().getAccessFlags()));
                        return dexFile;
                    }
                }));
        ClassTransformationTask task = new ClassTransformationTask(opcodes,
                                                                   folder.newFolder(),
                                                                   dexFileLoader);
        task.setClasses(new DexBackedDexFile(opcodes, bytes).getClasses());
        assertSame(dexFile, task.call());
        assertTrue(task.getClasses().isEmpty());
        assertFalse(new File(srcPathCaptor.getValue()).exists());
    }

    @Test(expected = IOException.class)
    public void call_should_throw_IOException_if_the_cache_directory_cannot_be_created()
            throws IOException {
        new ClassTransformationTask(Opcodes.getDefault(), folder.newFile(), dexFileLoader).call();
    }

}

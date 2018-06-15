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
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFileHolderMapperTest {

    @InjectMocks
    private DexFileHolderMapper testTarget;

    @Mock
    private ClassNameFilter classNameFilter;
    @Mock
    private DexFileOpener dexFileOpener;
    @Mock
    private RunnableFuture<dalvik.system.DexFile> dexFileFuture;

    @Captor
    private ArgumentCaptor<DexFile> dexFileCaptor;

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Test
    public void map_should_collect_DexFileHolders() throws Exception {
        Set<ClassDef> classes = new HashSet<>();
        int classCount = 101; // DexFileHolderMapper#MAX_CLASSES_PER_DEX_FILE + 1
        for (int i = 0; i < classCount; i++) {
            classes.add(new ImmutableClassDef("Lfoo/Bar" + i + ';',
                                              0,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null));
        }
        byte[] bytecode = DexPoolUtils.toBytecode(new ImmutableDexFile(Opcodes.getDefault(), classes));
        given(classNameFilter.accept(anyString())).willReturn(true);
        given(dexFileOpener.openDexFile(dexFileCaptor.capture())).willReturn(dexFileFuture);
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        testTarget.map(bytecode, holderMap);
        assertEquals(classCount, holderMap.size());
        assertEquals(2, new HashSet<>(holderMap.values()).size());
        List<DexFile> dexFiles = dexFileCaptor.getAllValues();
        assertEquals(2, dexFiles.size());
        assertEquals(100 /* = DexFileHolderMapper#MAX_CLASSES_PER_DEX_FILE */, dexFiles.get(0).getClasses().size());
        assertEquals(1, dexFiles.get(1).getClasses().size());
    }

}

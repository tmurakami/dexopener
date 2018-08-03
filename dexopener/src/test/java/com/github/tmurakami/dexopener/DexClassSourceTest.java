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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexClassSourceTest {

    @Mock(stubOnly = true)
    private DexClassFileFactory dexClassFileFactory;
    @Mock(stubOnly = true)
    private DexFileHolder dexFileHolder;
    @Mock(stubOnly = true)
    private ClassFile classFile;

    @SuppressWarnings("deprecation")
    @Test
    public void getClassFile_should_return_the_ClassFile_if_the_given_name_is_in_the_map_of_holders()
            throws Exception {
        dalvik.system.DexFile dexFile = new dalvik.system.DexFile("test");
        given(dexFileHolder.get()).willReturn(dexFile);
        String className = "foo.Bar";
        given(dexClassFileFactory.newClassFile(className, dexFile)).willReturn(classFile);
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        holderMap.put(className, dexFileHolder);
        assertSame(classFile, new DexClassSource(holderMap, dexClassFileFactory).getClassFile(className));
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_is_not_in_the_map_of_holders()
            throws Exception {
        assertNull(new DexClassSource(Collections.<String, DexFileHolder>emptyMap(),
                                      dexClassFileFactory).getClassFile("foo.Bar"));
    }

}

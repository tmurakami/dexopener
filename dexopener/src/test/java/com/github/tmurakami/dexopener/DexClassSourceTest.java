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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexClassSourceTest {

    @Mock(stubOnly = true)
    private DexFileHolder dexFileHolder;
    @Mock(stubOnly = true)
    private dalvik.system.DexFile dexFile;

    @Test
    public void getClassFile_should_return_the_ClassFile_if_the_given_name_is_in_the_map_of_holders()
            throws Exception {
        given(dexFileHolder.get()).willReturn(dexFile);
        final String className = "foo.Bar";
        given(dexFile.entries()).willAnswer(new Answer<Enumeration<String>>() {
            @Override
            public Enumeration<String> answer(InvocationOnMock invocation) {
                return Collections.enumeration(Collections.singletonList(className));
            }
        });
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        holderMap.put(className, dexFileHolder);
        assertNotNull(new DexClassSource(holderMap).getClassFile(className));
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_is_not_in_the_map_of_holders()
            throws Exception {
        assertNull(new DexClassSource(Collections.<String, DexFileHolder>emptyMap()).getClassFile("foo.Bar"));
    }

}

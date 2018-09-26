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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexClassSourceTest {

    @Mock
    private RunnableFuture<dalvik.system.DexFile> future;
    @Mock(stubOnly = true)
    private dalvik.system.DexFile dexFile;

    @Test
    public void getClassFile_should_return_the_ClassFile_if_the_given_name_is_in_the_map_of_futures()
            throws Exception {
        given(future.get()).willReturn(dexFile);
        final String className = "foo.Bar";
        given(dexFile.entries()).willAnswer(new Answer<Enumeration<String>>() {
            @Override
            public Enumeration<String> answer(InvocationOnMock invocation) {
                return Collections.enumeration(Collections.singletonList(className));
            }
        });
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        futureMap.put(className, future);
        assertNotNull(new DexClassSource(futureMap).getClassFile(className));
        then(future).should().run();
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_is_not_in_the_map_of_futures()
            throws IOException {
        assertNull(new DexClassSource(Collections.<String, RunnableFuture<dalvik.system.DexFile>>emptyMap())
                           .getClassFile("foo.Bar"));
    }

    @Test
    public void getClassFile_should_return_the_ClassFile_even_if_the_thread_is_interrupted()
            throws Exception {
        given(future.get()).willReturn(dexFile);
        final String className = "foo.Bar";
        given(dexFile.entries()).willAnswer(new Answer<Enumeration<String>>() {
            @Override
            public Enumeration<String> answer(InvocationOnMock invocation) {
                return Collections.enumeration(Collections.singletonList(className));
            }
        });
        given(future.get()).willThrow(new InterruptedException()).willReturn(dexFile);
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        futureMap.put(className, future);
        assertNotNull(new DexClassSource(futureMap).getClassFile(className));
        assertTrue(Thread.interrupted());
        then(future).should().run();
    }

    @Test(expected = IOException.class)
    public void getClassFile_should_throw_IOException_if_an_io_error_occurs_during_future_execution()
            throws Exception {
        given(future.get()).willThrow(new ExecutionException(new IOException()));
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        String className = "foo.Bar";
        futureMap.put(className, future);
        new DexClassSource(futureMap).getClassFile(className);
    }

    @Test(expected = RuntimeException.class)
    public void getClassFile_should_throw_RuntimeException_if_a_runtime_error_occurs_during_future_execution()
            throws Exception {
        given(future.get()).willThrow(new ExecutionException(new RuntimeException()));
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        String className = "foo.Bar";
        futureMap.put(className, future);
        new DexClassSource(futureMap).getClassFile(className);
    }

    @Test(expected = Error.class)
    public void getClassFile_should_throw_Error_if_an_error_occurs_during_future_execution()
            throws Exception {
        given(future.get()).willThrow(new ExecutionException(new Error()));
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        String className = "foo.Bar";
        futureMap.put(className, future);
        new DexClassSource(futureMap).getClassFile(className);
    }

    @Test(expected = UndeclaredThrowableException.class)
    public void getClassFile_should_throw_UndeclaredThrowableException_if_an_unexpected_error_occurs_during_future_execution()
            throws Exception {
        given(future.get()).willThrow(new ExecutionException(new Throwable()));
        Map<String, RunnableFuture<dalvik.system.DexFile>> futureMap = new HashMap<>();
        String className = "foo.Bar";
        futureMap.put(className, future);
        new DexClassSource(futureMap).getClassFile(className);
    }

}

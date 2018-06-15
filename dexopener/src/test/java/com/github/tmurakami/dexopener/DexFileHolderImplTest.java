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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFileHolderImplTest {

    private DexFileHolderImpl testTarget;

    @Mock
    private RunnableFuture<dalvik.system.DexFile> dexFileFuture;

    @Before
    public void setUp() {
        testTarget = new DexFileHolderImpl();
        testTarget.setDexFileFuture(dexFileFuture);
    }

    @Test
    public void get_should_return_the_dex_file() throws Exception {
        dalvik.system.DexFile dexFile = new dalvik.system.DexFile("test");
        given(dexFileFuture.get()).willReturn(dexFile);
        assertSame(dexFile, testTarget.get());
        then(dexFileFuture).should().run();
    }

    @Test
    public void get_should_return_the_dex_file_even_if_the_thread_is_interrupted()
            throws Exception {
        dalvik.system.DexFile dexFile = new dalvik.system.DexFile("test");
        given(dexFileFuture.get()).willThrow(new InterruptedException()).willReturn(dexFile);
        assertSame(dexFile, testTarget.get());
        assertTrue(Thread.interrupted());
        then(dexFileFuture).should().run();
    }

    @Test(expected = IOException.class)
    public void get_should_throw_IOException_if_an_io_error_occurs_during_future_execution()
            throws Exception {
        given(dexFileFuture.get()).willThrow(new ExecutionException(new IOException()));
        testTarget.get();
    }

    @Test(expected = RuntimeException.class)
    public void get_should_throw_IOException_if_a_runtime_error_occurs_during_future_execution()
            throws Exception {
        given(dexFileFuture.get()).willThrow(new ExecutionException(new RuntimeException()));
        testTarget.get();
    }

    @Test(expected = Error.class)
    public void get_should_throw_IOException_if_an_error_occurs_during_future_execution()
            throws Exception {
        given(dexFileFuture.get()).willThrow(new ExecutionException(new Error()));
        testTarget.get();
    }

    @Test(expected = UndeclaredThrowableException.class)
    public void get_should_throw_IOException_if_an_unexpected_error_occurs_during_future_execution()
            throws Exception {
        given(dexFileFuture.get()).willThrow(new ExecutionException(new Throwable()));
        testTarget.get();
    }

}

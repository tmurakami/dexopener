/*
 * Copyright 2017 Tsuyoshi Murakami
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOError;
import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InjectorClassLoaderTest {

    private InjectorClassLoader testTarget;

    @Mock
    private ClassSource source;
    @Mock
    private ClassFile classFile;

    private ClassLoader injectionTarget = new ClassLoader(null) {
    };

    @Before
    public void setUp() {
        testTarget = new InjectorClassLoader(null, source, injectionTarget);
    }

    @Test
    public void findClass_should_return_the_Class_with_the_given_name() throws Exception {
        given(source.getClassFile("foo.Bar")).willReturn(classFile);
        Class<?> c = getClass();
        given(classFile.toClass(injectionTarget)).willReturn(c);
        assertSame(c, testTarget.findClass("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void findClass_should_throw_IllegalStateException_if_called_recursively()
            throws Exception {
        given(source.getClassFile("foo.Bar")).willReturn(classFile);
        given(classFile.toClass(injectionTarget)).will(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                testTarget.findClass("foo.Bar");
                return null;
            }
        });
        testTarget.findClass("foo.Bar");
    }

    @Test(expected = ClassNotFoundException.class)
    public void findClass_should_throw_ClassNotFoundException_if_the_given_name_belongs_to_my_package()
            throws Exception {
        testTarget.findClass(InjectorClassLoaderTest.class.getName());
    }

    @Test(expected = ClassNotFoundException.class)
    public void findClass_should_throw_ClassNotFoundException_if_no_Class_with_the_given_name_can_be_found()
            throws Exception {
        testTarget.findClass("foo.Bar");
        then(source).should().getClassFile("foo.Bar");
    }

    @Test(expected = IOError.class)
    public void findClass_should_throw_IOError_if_an_IO_error_occurs_while_getting_the_ClassFile()
            throws Exception {
        given(source.getClassFile("foo.Bar")).willThrow(IOException.class);
        testTarget.findClass("foo.Bar");
    }

}

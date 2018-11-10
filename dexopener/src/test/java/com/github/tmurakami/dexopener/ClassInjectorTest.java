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
import org.mockito.junit.MockitoJUnitRunner;

import test.MyClass;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClassInjectorTest {

    @Mock
    private ClassPath classPath;

    @Test
    public void should_get_the_Class_with_the_given_name() throws ClassNotFoundException {
        ClassLoader loader = new ClassLoader() {
        };
        given(classPath.loadClass("foo.Bar", loader)).willReturn(MyClass.class);
        ClassLoaderHelper.setParent(loader, new ClassInjector(loader, classPath));
        assertSame(MyClass.class, loader.loadClass("foo.Bar"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void should_throw_ClassNotFoundException_if_the_given_name_could_not_be_found()
            throws ClassNotFoundException {
        ClassLoader loader = new ClassLoader() {
        };
        ClassLoaderHelper.setParent(loader, new ClassInjector(loader, classPath));
        try {
            loader.loadClass("foo.Bar");
        } finally {
            then(classPath).should().loadClass("foo.Bar", loader);
        }
    }

}

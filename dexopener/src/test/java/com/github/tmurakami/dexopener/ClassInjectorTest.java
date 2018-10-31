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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClassInjectorTest {

    @InjectMocks
    private ClassInjector testTarget;

    @Mock
    private InjectorClassLoaderFactory injectorClassLoaderFactory;
    @Mock
    private InjectorClassLoader injectorClassLoader;

    @Test
    public void into_should_replace_the_parent_with_the_InjectorClassLoader() {
        ClassLoader target = new ClassLoader(null) {
        };
        given(injectorClassLoaderFactory.newInjectorClassLoader(null, target)).willReturn(injectorClassLoader);
        testTarget.into(target);
        assertSame(injectorClassLoader, target.getParent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void into_should_throw_IllegalArgumentException_if_the_target_is_an_InjectorClassLoader() {
        testTarget.into(mock(InjectorClassLoader.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void into_should_throw_IllegalArgumentException_if_the_parent_is_an_InjectorClassLoader() {
        testTarget.into(new ClassLoader(mock(InjectorClassLoader.class)) {
        });
    }

}

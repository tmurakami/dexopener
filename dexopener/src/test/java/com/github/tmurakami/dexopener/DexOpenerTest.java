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

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class DexOpenerTest {

    @Mock(stubOnly = true)
    private Instrumentation instrumentation;
    @Mock(stubOnly = true)
    private Context context;

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_target_context_is_null() {
        given(instrumentation.getTargetContext()).willReturn(null);
        DexOpener.install(instrumentation);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_Application_has_been_created() {
        given(instrumentation.getTargetContext()).willReturn(context);
        given(context.getApplicationContext()).willReturn(new Application());
        DexOpener.install(instrumentation);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_already_installed() {
        ClassInjector injector = mock(ClassInjector.class, withSettings().stubOnly());
        given(instrumentation.getTargetContext()).willReturn(context);
        ClassLoader loader = new ClassLoader(injector) {
        };
        given(context.getClassLoader()).willReturn(loader);
        DexOpener.install(instrumentation);
    }

}

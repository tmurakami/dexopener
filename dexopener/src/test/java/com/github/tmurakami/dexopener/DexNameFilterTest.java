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

import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import test.MyClass;

import static org.junit.Assert.assertSame;

@RunWith(Parameterized.class)
public class DexNameFilterTest {

    private static final boolean ALLOW = true;
    private static final boolean DENY = false;

    private final DexNameFilter testTarget = new DexNameFilter("test", MyClass.class);

    private final String className;
    private final boolean expected;

    public DexNameFilterTest(String className, boolean expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[]{"C", DENY},
                             new Object[]{"test.BR", DENY},
                             new Object[]{"test.BuildConfig", DENY},
                             new Object[]{"test.R", DENY},
                             new Object[]{"test.R$string", DENY},
                             new Object[]{MyClass.class.getName(), DENY},
                             new Object[]{"android.databinding.DataBinderMapper", ALLOW},
                             new Object[]{"android.databinding.DataBindingComponent", ALLOW},
                             new Object[]{"android.databinding.DataBindingUtil", ALLOW},
                             new Object[]{"android.databinding.generated.C", ALLOW},
                             new Object[]{"test.Foo", ALLOW});
    }

    @Test
    public void should_get_the_same_value_as_the_expected_value() {
        assertSame(expected, testTarget.apply(ReflectionUtils.javaToDexName(className)));
    }

}

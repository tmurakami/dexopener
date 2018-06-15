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
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertSame;

@RunWith(Parameterized.class)
public class ClassNameFilterTest {

    private static final boolean ALLOW = true;
    private static final boolean DENY = false;

    private final ClassNameFilter testTarget = new ClassNameFilter("foo.");

    private final String className;
    private final boolean expected;

    public ClassNameFilterTest(String className, boolean expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[]{"C", DENY},
                             new Object[]{"foo.BR", DENY},
                             new Object[]{"foo.BuildConfig", DENY},
                             new Object[]{"foo.R", DENY},
                             new Object[]{"foo.R$string", DENY},
                             new Object[]{"android.databinding.DataBinderMapper", ALLOW},
                             new Object[]{"android.databinding.DataBindingComponent", ALLOW},
                             new Object[]{"android.databinding.DataBindingUtil", ALLOW},
                             new Object[]{"android.databinding.generated.C", ALLOW},
                             new Object[]{"foo.Bar", ALLOW},
                             new Object[]{"foo.Bar$BR", ALLOW},
                             new Object[]{"foo.Bar$BuildConfig", ALLOW},
                             new Object[]{"foo.Bar$R", ALLOW},
                             new Object[]{"foo.Bar$R$string", ALLOW});
    }

    @Test
    public void accept_should_return_a_value_equal_to_the_expected_value() {
        assertSame(expected, testTarget.accept(className));
    }

}

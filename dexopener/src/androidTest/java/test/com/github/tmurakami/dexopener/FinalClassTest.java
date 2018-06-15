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

package test.com.github.tmurakami.dexopener;

import org.junit.Test;

import static java.lang.reflect.Modifier.isFinal;
import static org.junit.Assert.assertFalse;

public class FinalClassTest {

    @Test
    public void runner_should_remove_all_final_modifiers() throws Exception {
        assertFalse(isFinal(FinalClass.class.getModifiers()));
        assertFalse(isFinal(FinalClass.class.getDeclaredMethod("doIt").getModifiers()));
    }

    private static final class FinalClass {
        @SuppressWarnings("unused")
        final void doIt() {
        }
    }

}

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
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static java.lang.reflect.Modifier.isFinal;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class FinalClassTest {

    @Test
    public void should_remove_all_final_modifiers() throws NoSuchMethodException {
        assertFalse(isFinal(FinalClass.class.getModifiers()));
        assertFalse(isFinal(FinalClass.class.getDeclaredMethod("doIt").getModifiers()));
    }

    @Test
    public void should_load_the_final_class_as_a_non_final_class() throws ClassNotFoundException {
        ClassLoader loader = getClass().getClassLoader();
        assertNotNull(loader);
        Class<?> c2 = loader.loadClass("test.com.github.tmurakami.dexopener.FinalClassTest$FinalClass2");
        assertFalse(isFinal(c2.getModifiers()));
        // On Android, it is possible to load a class with the slash-separated name via a class
        // loader.
        Class<?> c3 = loader.loadClass("test/com/github/tmurakami/dexopener/FinalClassTest$FinalClass3");
        assertFalse(isFinal(c3.getModifiers()));
    }

    private static final class FinalClass {
        final void doIt() {
        }
    }

    private static final class FinalClass2 {
    }

    @SuppressWarnings("unused")
    private static final class FinalClass3 {
    }

}

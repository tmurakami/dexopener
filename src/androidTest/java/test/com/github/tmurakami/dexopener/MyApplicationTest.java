package test.com.github.tmurakami.dexopener;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertFalse;

public class MyApplicationTest {
    @Test
    public void finalModifierIsRemoved() throws Exception {
        assertFalse(Modifier.isFinal(MyApplication.class.getModifiers()));
        assertFalse(Modifier.isFinal(MyApplication.class.getMethod("onCreate").getModifiers()));
    }
}

package test.com.github.tmurakami.dexopener;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertFalse;

public class FinalClassTest {

    @Test
    public void DexOpener_should_remove_all_final_modifiers() throws Exception {
        assertFalse(Modifier.isFinal(FinalClass.class.getModifiers()));
        assertFalse(Modifier.isFinal(FinalClass.class.getDeclaredMethod("doIt").getModifiers()));
    }

    private static final class FinalClass {
        @SuppressWarnings("unused")
        final void doIt() {
        }
    }

}

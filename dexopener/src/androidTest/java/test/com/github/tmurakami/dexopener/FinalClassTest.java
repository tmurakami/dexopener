package test.com.github.tmurakami.dexopener;

import org.junit.Test;

import static java.lang.reflect.Modifier.isFinal;
import static org.junit.Assert.assertFalse;

public class FinalClassTest {

    @Test
    public void should_remove_all_final_modifiers() throws Exception {
        assertFalse(isFinal(FinalClass.class.getModifiers()));
        assertFalse(isFinal(FinalClass.class.getDeclaredMethod("doIt").getModifiers()));
    }

    private static final class FinalClass {
        @SuppressWarnings("unused")
        final void doIt() {
        }
    }

}

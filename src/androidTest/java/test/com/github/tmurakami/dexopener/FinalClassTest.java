package test.com.github.tmurakami.dexopener;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class FinalClassTest {

    @Test
    public void testFinalClassMock() {
        C mock = mock(C.class);
        Object o = new Object();
        given(mock.doIt()).willReturn(o);
        assertEquals(o, mock.doIt());
    }

    @SuppressWarnings("WeakerAccess")
    static final class C {
        final Object doIt() {
            return null;
        }
    }

}

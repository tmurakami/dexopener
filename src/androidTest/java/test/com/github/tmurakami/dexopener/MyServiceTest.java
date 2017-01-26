package test.com.github.tmurakami.dexopener;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

public class MyServiceTest {
    @Test
    public void doIt() throws Exception {
        MyService service = mock(MyService.class);
        willReturn("test").given(service).doIt();
        assertEquals("test", service.doIt());
    }
}

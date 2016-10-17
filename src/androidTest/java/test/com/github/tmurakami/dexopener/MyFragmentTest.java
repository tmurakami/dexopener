package test.com.github.tmurakami.dexopener;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.app.FragmentActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.willReturn;

public class MyFragmentTest {

    @Rule
    public final ActivityTestRule<FragmentActivity> rule = new ActivityTestRule<>(FragmentActivity.class, true, false);

    @Mock
    MyService service;

    @InjectMocks
    MyFragment target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOnCreate() {
        Object o = new Object();
        willReturn(o).given(service).doIt();
        rule.launchActivity(null).getSupportFragmentManager().beginTransaction().add(target, null).commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertEquals(o, target.result);
    }

}

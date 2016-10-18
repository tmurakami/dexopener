package test.com.github.tmurakami.dexopener;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.intercepting.SingleActivityFactory;

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
    public final ActivityTestRule<MyActivity> rule = new ActivityTestRule<>(new SingleActivityFactory<MyActivity>(MyActivity.class) {
        @Override
        protected MyActivity create(Intent intent) {
            return new MyActivity(target);
        }
    }, true, false);

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
        rule.launchActivity(null);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertEquals(o, target.result);
    }

}

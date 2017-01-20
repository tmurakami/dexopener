package test.com.github.tmurakami.dexopener;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleCallback;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.willReturn;

public class MyActivityTest implements ActivityLifecycleCallback {

    @Rule
    public final ActivityTestRule<MyActivity> rule = new ActivityTestRule<>(MyActivity.class, true, false);

    @Mock
    MyService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(this);
    }

    @After
    public void tearDown() {
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this);
    }

    @Test
    public void testOnCreate() {
        Object o = new Object();
        willReturn(o).given(service).doIt();
        assertEquals(o, rule.launchActivity(null).result);
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        if (activity instanceof MyActivity && stage == Stage.PRE_ON_CREATE) {
            ((MyActivity) activity).service = service;
        }
    }

}

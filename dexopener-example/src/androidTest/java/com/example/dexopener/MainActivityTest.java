package com.example.dexopener;

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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;

public class MainActivityTest implements ActivityLifecycleCallback {

    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Mock
    MyService service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(this);
    }

    @After
    public void tearDown() throws Exception {
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this);
    }

    @Test
    public void onCreate() throws Exception {
        String s = "test";
        given(service.getString(isA(MainActivity.class))).willReturn(s);
        assertEquals(s, rule.launchActivity(null).getTitle());
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        if (activity instanceof MainActivity && stage == Stage.PRE_ON_CREATE) {
            ((MainActivity) activity).service = service;
        }
    }

}

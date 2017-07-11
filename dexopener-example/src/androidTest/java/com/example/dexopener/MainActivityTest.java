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
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.verify;

public class MainActivityTest implements ActivityLifecycleCallback {

    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private MainActivityDelegate delegate;

    @Before
    public void setUp() throws Exception {
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(this);
        activityRule.launchActivity(null);
    }

    @After
    public void tearDown() throws Exception {
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this);
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        if (activity instanceof MainActivity && stage == Stage.PRE_ON_CREATE) {
            ((MainActivity) activity).delegate = delegate;
        }
    }

    @Test
    public void onCreate_should_simply_call_MainActivityDelegate_onCreate() throws Exception {
        verify(delegate).onCreate(null);
    }

}

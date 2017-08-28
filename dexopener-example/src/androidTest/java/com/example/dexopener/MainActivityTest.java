package com.example.dexopener;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.intercepting.SingleActivityFactory;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(new SingleActivityFactory<MainActivity>(MainActivity.class) {
                @Override
                protected MainActivity create(Intent intent) {
                    MainActivity testTarget = new MainActivity();
                    testTarget.delegate = mock(MainActivityDelegate.class);
                    return testTarget;
                }
            }, /* initialTouchMode */ false, /* launchActivity */ false);

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Test
    public void onCreate_should_simply_call_MainActivityDelegate_onCreate() throws Exception {
        activityRule.launchActivity(null);
        verify(activityRule.getActivity().delegate).onCreate(null);
    }

}

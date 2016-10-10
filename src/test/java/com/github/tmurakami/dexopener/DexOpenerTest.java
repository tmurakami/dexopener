package com.github.tmurakami.dexopener;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.test.internal.runner.TestRequestBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.objenesis.ObjenesisStd;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DexOpenerTest {

    @Mock
    DexOpenerDelegate delegate;

    @InjectMocks
    DexOpener target = new ObjenesisStd(false).newInstance(DexOpener.class);

    @Test
    public void testCallActivityOnCreateActivityBundle() {
        Activity activity = new Activity();
        Bundle bundle = new Bundle();
        target.callActivityOnCreate(activity, bundle);
        then(delegate).should().callActivityOnCreate(activity, bundle);
    }

    @Test
    public void testCallActivityOnCreateActivityBundlePersistableBundle() {
        Activity activity = new Activity();
        Bundle icicle = new Bundle();
        PersistableBundle persistentState = mock(PersistableBundle.class);
        target.callActivityOnCreate(activity, icicle, persistentState);
        then(delegate).should().callActivityOnCreate(activity, icicle, persistentState);
    }

    @Test
    public void testCreateTestRequestBuilder() {
        Instrumentation instr = new Instrumentation();
        Bundle arguments = new Bundle();
        TestRequestBuilder builder = mock(TestRequestBuilder.class);
        given(delegate.createTestRequestBuilder(instr, arguments)).willReturn(builder);
        assertSame(builder, target.createTestRequestBuilder(instr, arguments));
    }

    @Test
    public void testGetContext() {
        Context context = mock(Context.class);
        given(delegate.getContext()).willReturn(context);
        assertSame(context, target.getContext());
    }

    @Test
    public void testGetTargetContext() {
        Context context = mock(Context.class);
        given(delegate.getTargetContext()).willReturn(context);
        assertSame(context, target.getTargetContext());
    }

    @Test
    public void testNewActivity()
            throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ClassLoader cl = getClass().getClassLoader();
        Intent intent = new Intent();
        Activity activity = new Activity();
        given(delegate.newActivity(cl, "test", intent)).willReturn(activity);
        assertSame(activity, target.newActivity(cl, "test", intent));
    }

    @Test
    public void testNewApplication()
            throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ClassLoader cl = getClass().getClassLoader();
        Context context = mock(Context.class);
        Application application = new Application();
        given(delegate.newApplication(cl, "test", context)).willReturn(application);
        assertSame(application, target.newApplication(cl, "test", context));
    }

    @Test
    public void testOnStart() {
        target.onStart();
        then(delegate).should().onStart();
    }

}

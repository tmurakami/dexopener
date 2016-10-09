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
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DexOpenerDelegateImplTest {

    @Mock
    DexOpenerDelegate delegate;
    @Mock
    DexOpenerHelper helper;

    @InjectMocks
    DexOpenerDelegateImpl target;

    @Test
    public void testCallActivityOnCreateActivityBundle() {
        Activity activity = mock(Activity.class);
        final Context base = mock(Context.class);
        given(activity.getBaseContext()).willReturn(base);
        Bundle bundle = new Bundle();
        final ClassLoader classLoader = mock(ClassLoader.class);
        target.classLoader = classLoader;
        target.callActivityOnCreate(activity, bundle);
        InOrder inOrder = inOrder(helper, delegate);
        then(helper).should(inOrder)
                .setBaseContext(eq(activity), argThat(new ArgumentMatcher<Context>() {
                    @Override
                    public boolean matches(Context c) {
                        return c.getClassLoader() == classLoader
                                && c instanceof InternalContextWrapper
                                && ((InternalContextWrapper) c).getBaseContext() == base;
                    }
                }));
        then(delegate).should(inOrder).callActivityOnCreate(activity, bundle);
    }

    @Test
    public void testCallActivityOnCreateActivityBundlePersistableBundle() {
        Activity activity = mock(Activity.class);
        final Context base = mock(Context.class);
        given(activity.getBaseContext()).willReturn(base);
        Bundle icicle = new Bundle();
        PersistableBundle persistentState = mock(PersistableBundle.class);
        final ClassLoader classLoader = mock(ClassLoader.class);
        target.classLoader = classLoader;
        target.callActivityOnCreate(activity, icicle, persistentState);
        InOrder inOrder = inOrder(helper, delegate);
        then(helper).should(inOrder)
                .setBaseContext(eq(activity), argThat(new ArgumentMatcher<Context>() {
                    @Override
                    public boolean matches(Context c) {
                        return c.getClassLoader() == classLoader
                                && c instanceof InternalContextWrapper
                                && ((InternalContextWrapper) c).getBaseContext() == base;
                    }
                }));
        then(delegate).should(inOrder).callActivityOnCreate(activity, icicle, persistentState);
    }

    @Test
    public void testCallApplicationOnCreate() {
        Application app = mock(Application.class);
        final Context base = mock(Context.class);
        given(app.getBaseContext()).willReturn(base);
        final ClassLoader classLoader = mock(ClassLoader.class);
        target.classLoader = classLoader;
        target.callApplicationOnCreate(app);
        InOrder inOrder = inOrder(helper, delegate);
        then(helper).should(inOrder)
                .setBaseContext(eq(app), argThat(new ArgumentMatcher<Context>() {
                    @Override
                    public boolean matches(Context c) {
                        return c.getClassLoader() == classLoader
                                && c instanceof InternalContextWrapper
                                && ((InternalContextWrapper) c).getBaseContext() == base;
                    }
                }));
        then(delegate).should(inOrder).callApplicationOnCreate(app);
    }

    @Test
    public void testCreateTestRequestBuilder() {
        Instrumentation instr = new Instrumentation();
        Bundle arguments = new Bundle();
        TestRequestBuilder builder = mock(TestRequestBuilder.class);
        given(delegate.createTestRequestBuilder(instr, arguments)).willReturn(builder);
        ClassLoader classLoader = mock(ClassLoader.class);
        given(builder.setClassLoader(classLoader)).willReturn(builder);
        target.classLoader = classLoader;
        assertSame(builder, target.createTestRequestBuilder(instr, arguments));
    }

    @Test
    public void testGetContext() {
        Context context = mock(Context.class);
        target.context = context;
        assertSame(context, target.getContext());
    }

    @Test
    public void testGetTargetContext() {
        Context context = mock(Context.class);
        target.targetContext = context;
        assertSame(context, target.getTargetContext());
    }

    @Test
    public void testNewActivity()
            throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ClassLoader classLoader = mock(ClassLoader.class);
        Intent intent = new Intent();
        Activity activity = new Activity();
        given(delegate.newActivity(classLoader, "test", intent)).willReturn(activity);
        ClassLoader cl = getClass().getClassLoader();
        target.classLoader = classLoader;
        assertSame(activity, target.newActivity(cl, "test", intent));
    }

    @Test
    public void testNewApplication() throws Exception {
        Context context = mock(Context.class);
        given(delegate.getContext()).willReturn(context);
        given(context.getPackageCodePath()).willReturn("test.apk");
        Context targetContext = mock(Context.class);
        given(delegate.getTargetContext()).willReturn(targetContext);
        given(targetContext.getPackageCodePath()).willReturn("target.apk");
        File cacheDir = new File("");
        given(targetContext.getDir("dexopener", Context.MODE_PRIVATE)).willReturn(cacheDir);
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader classLoader = mock(ClassLoader.class);
        given(helper.newClassLoader("target.apk", "test.apk", cacheDir, cl)).willReturn(classLoader);
        Application application = new Application();
        given(delegate.newApplication(classLoader, "test", context)).willReturn(application);
        assertSame(application, target.newApplication(cl, "test", context));
    }

    @Test
    public void testOnStart() {
        ClassLoader classLoader = mock(ClassLoader.class);
        target.classLoader = classLoader;
        target.onStart();
        InOrder inOrder = inOrder(helper, delegate);
        then(helper).should(inOrder).setContextClassLoader(Thread.currentThread(), classLoader);
        then(delegate).should(inOrder).onStart();
    }

}

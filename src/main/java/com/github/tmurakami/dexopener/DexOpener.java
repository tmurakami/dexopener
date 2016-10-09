package com.github.tmurakami.dexopener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.test.internal.runner.TestRequestBuilder;
import android.support.test.runner._AndroidJUnitRunner;

@SuppressLint("NewApi")
public final class DexOpener extends _AndroidJUnitRunner {

    private DexOpenerDelegate delegate = newAndroidJUnitRunnerDelegate();

    @Override
    public void callActivityOnCreate(Activity activity, Bundle bundle) {
        delegate.callActivityOnCreate(activity, bundle);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        delegate.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        delegate.callApplicationOnCreate(app);
    }

    @Override
    protected TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments) {
        return delegate.createTestRequestBuilder(instr, arguments);
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public Context getTargetContext() {
        return delegate.getTargetContext();
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return delegate.newActivity(cl, className, intent);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return delegate.newApplication(cl, className, context);
    }

    @Override
    public void onStart() {
        delegate.onStart();
    }

    private DexOpenerDelegate newAndroidJUnitRunnerDelegate() {
        ClassNameFilter filter = new ClassNameFilterImpl();
        return new DexOpenerDelegateImpl(
                new SuperCalls(),
                new DexOpenerHelperImpl(new ClassesJarGeneratorImpl(filter), new ClassLoaderFactoryImpl(filter)));
    }

    private class SuperCalls implements DexOpenerDelegate {

        @Override
        public Application newApplication(ClassLoader cl, String className, Context context)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            return DexOpener.super.newApplication(cl, className, context);
        }

        @Override
        public Activity newActivity(ClassLoader cl, String className, Intent intent)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            return DexOpener.super.newActivity(cl, className, intent);
        }

        @Override
        public void callApplicationOnCreate(Application app) {
            DexOpener.super.callApplicationOnCreate(app);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle bundle) {
            DexOpener.super.callActivityOnCreate(activity, bundle);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
            DexOpener.super.callActivityOnCreate(activity, icicle, persistentState);
        }

        @Override
        public void onStart() {
            DexOpener.super.onStart();
        }

        @Override
        public Context getContext() {
            return DexOpener.super.getContext();
        }

        @Override
        public Context getTargetContext() {
            return DexOpener.super.getTargetContext();
        }

        @Override
        public TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments) {
            return DexOpener.super.createTestRequestBuilder(instr, arguments);
        }

    }

}

package com.github.tmurakami.dexopener;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.VisibleForTesting;
import android.support.test.internal.runner.TestRequestBuilder;

import java.io.File;
import java.io.IOException;

final class DexOpenerDelegateImpl implements DexOpenerDelegate {

    private final DexOpenerDelegate delegate;
    private final DexOpenerHelper helper;

    @VisibleForTesting
    ClassLoader classLoader;
    @VisibleForTesting
    Context context;
    @VisibleForTesting
    Context targetContext;

    DexOpenerDelegateImpl(DexOpenerDelegate delegate, DexOpenerHelper helper) {
        this.delegate = delegate;
        this.helper = helper;
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle bundle) {
        replaceBaseContext(activity);
        delegate.callActivityOnCreate(activity, bundle);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        replaceBaseContext(activity);
        delegate.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        replaceBaseContext(app);
        delegate.callApplicationOnCreate(app);
    }

    @Override
    public TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments) {
        return delegate.createTestRequestBuilder(instr, arguments).setClassLoader(classLoader);
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Context getTargetContext() {
        return targetContext;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return delegate.newActivity(classLoader, className, intent);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            init(cl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return delegate.newApplication(classLoader, className, context);
    }

    @Override
    public void onStart() {
        helper.setContextClassLoader(Thread.currentThread(), classLoader);
        delegate.onStart();
    }

    private void init(ClassLoader cl) throws IOException {
        Context context = delegate.getContext();
        Context targetContext = delegate.getTargetContext();
        String apkPath = targetContext.getPackageCodePath();
        String testApkPath = context.getPackageCodePath();
        File cacheDir = targetContext.getDir("dexopener", Context.MODE_PRIVATE);
        ClassLoader classLoader = helper.newClassLoader(apkPath, testApkPath, cacheDir, cl);
        this.classLoader = classLoader;
        this.context = new InternalContextWrapper(context, classLoader);
        this.targetContext = new InternalContextWrapper(targetContext, classLoader);
    }

    private void replaceBaseContext(ContextWrapper context) {
        Context base = context.getBaseContext();
        if (base instanceof InternalContextWrapper) {
            return;
        }
        helper.setBaseContext(context, new InternalContextWrapper(base, classLoader));
    }

}

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
    private final DexOpenerDelegateHelper helper;

    @VisibleForTesting
    ClassLoader classLoader;
    @VisibleForTesting
    Context context;
    @VisibleForTesting
    Context targetContext;

    DexOpenerDelegateImpl(DexOpenerDelegate delegate, DexOpenerDelegateHelper helper) {
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
        ClassLoader loader = classLoader;
        return delegate.newApplication(loader, className, new InternalContextWrapper(context, loader));
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
        ClassLoader loader = helper.newClassLoader(apkPath, testApkPath, cacheDir, cl);
        this.classLoader = loader;
        this.context = new InternalContextWrapper(context, loader);
        this.targetContext = new InternalContextWrapper(context, loader);
    }

    private void replaceBaseContext(ContextWrapper context) {
        helper.setBaseContext(context, new InternalContextWrapper(context.getBaseContext(), classLoader));
    }

    private static class InternalContextWrapper extends ContextWrapper {

        private final Context base;
        private final ClassLoader classLoader;

        InternalContextWrapper(Context base, ClassLoader classLoader) {
            super(base);
            this.base = base;
            this.classLoader = classLoader;
        }

        @Override
        public Context getBaseContext() {
            return base;
        }

        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }

    }

}

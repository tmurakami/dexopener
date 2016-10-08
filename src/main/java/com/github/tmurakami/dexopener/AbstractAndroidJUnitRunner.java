package com.github.tmurakami.dexopener;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.test.internal.runner.TestRequestBuilder;
import android.support.test.runner._AndroidJUnitRunner;

abstract class AbstractAndroidJUnitRunner extends _AndroidJUnitRunner {

    private ClassLoader classLoader;
    private Context context;
    private Context targetContext;

    @SuppressWarnings("WeakerAccess")
    protected AbstractAndroidJUnitRunner() {
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        init(cl);
        return super.newApplication(classLoader, className, context);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newActivity(classLoader, className, intent);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        replaceBaseContext(app);
        super.callApplicationOnCreate(app);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle bundle) {
        replaceBaseContext(activity);
        super.callActivityOnCreate(activity, bundle);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        replaceBaseContext(activity);
        super.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void onStart() {
        Thread.currentThread().setContextClassLoader(classLoader);
        super.onStart();
    }

    @Override
    public Context getContext() {
        if (context == null) {
            throw new IllegalStateException(getClass().getName() + " has not been initialized yet");
        }
        return context;
    }

    @Override
    public Context getTargetContext() {
        if (targetContext == null) {
            throw new IllegalStateException(getClass().getName() + " has not been initialized yet");
        }
        return targetContext;
    }

    @Override
    protected TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments) {
        return super.createTestRequestBuilder(instr, arguments).setClassLoader(classLoader);
    }

    abstract ClassLoader newClassLoader(Context context, Context targetContext, ClassLoader parent) throws Exception;

    private void init(ClassLoader classLoader)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Context context = super.getContext();
        Context targetContext = super.getTargetContext();
        ClassLoader loader;
        try {
            loader = newClassLoader(context, targetContext, classLoader);
        } catch (RuntimeException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.classLoader = loader;
        this.context = new InternalContextWrapper(context, loader);
        this.targetContext = new InternalContextWrapper(targetContext, loader);
    }

    private void replaceBaseContext(ContextWrapper context) {
        Context base = context.getBaseContext();
        if (base instanceof InternalContextWrapper) {
            return;
        }
        ContextWrapperHelper.setBaseContext(context, new InternalContextWrapper(base, classLoader));
    }

}

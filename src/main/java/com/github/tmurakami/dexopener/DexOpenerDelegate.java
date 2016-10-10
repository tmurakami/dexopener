package com.github.tmurakami.dexopener;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.test.internal.runner.TestRequestBuilder;

interface DexOpenerDelegate {

    void callActivityOnCreate(Activity activity, Bundle bundle);

    void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState);

    TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments);

    Context getContext();

    Context getTargetContext();

    Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException;

    Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException;

    void onStart();

}

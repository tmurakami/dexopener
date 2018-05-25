package com.example.dexopener.using.dexopener;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;

import com.github.tmurakami.dexopener.DexOpener;

public class MyAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.install(this);
        return super.newApplication(cl, className, context);
    }
}

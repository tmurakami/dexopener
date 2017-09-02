package com.example.dexopener.using.application.id.suffix;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;

import com.github.tmurakami.dexopener.DexOpener;

public class JUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.builder(context)
                 .buildConfig(com.example.dexopener.BuildConfig.class) // Set the app's BuildConfig
                 .build()
                 .installTo(cl);
        return super.newApplication(cl, className, context);
    }
}

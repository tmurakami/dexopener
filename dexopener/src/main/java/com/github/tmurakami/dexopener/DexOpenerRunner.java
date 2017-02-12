package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;

import java.util.logging.Logger;

@Deprecated
public class DexOpenerRunner extends DexOpenerAndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Logger.getLogger(BuildConfig.APPLICATION_ID).warning("DexOpenerRunner is deprecated. Use DexOpenerAndroidJUnitRunner instead.");
        return super.newApplication(cl, className, context);
    }
}

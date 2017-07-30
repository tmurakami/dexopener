package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnitRunner;

public class JUnitRunner extends AndroidJUnitRunner {

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        newDexOpener(context).installTo(cl);
        return super.newApplication(cl, className, context);
    }

    @SuppressWarnings("deprecation")
    private static DexOpener newDexOpener(Context context) {
        return new DexOpenerImpl(context,
                                 new ClassNameFilterWrapper(new ClassNameFilter() {
                                     @Override
                                     public boolean accept(@NonNull String className) {
                                         return className.startsWith("test.com.github.tmurakami.dexopener.");
                                     }
                                 }),
                                 new DexFileLoader(),
                                 new DexClassSourceFactory(new DexClassFileFactory()));
    }

}

package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

@SuppressWarnings("WeakerAccess")
public class DexOpener extends AndroidJUnitRunner {

    private Installer installer = Installer.create();
    private SuperCalls superCalls = new SuperCallsImpl();

    private boolean initialized;

    @Override
    public void onCreate(Bundle arguments) {
        init();
        superCalls.onCreate(arguments);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        init();
        return superCalls.newApplication(cl, className, context);
    }

    private void init() {
        if (!initialized) {
            initialized = true;
            installer.install(superCalls.getTargetContext());
        }
    }

    interface SuperCalls {

        Context getTargetContext();

        void onCreate(Bundle arguments);

        Application newApplication(ClassLoader cl, String className, Context context)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException;

    }

    private class SuperCallsImpl implements SuperCalls {

        @Override
        public Context getTargetContext() {
            return DexOpener.super.getTargetContext();
        }

        @Override
        public void onCreate(Bundle arguments) {
            DexOpener.super.onCreate(arguments);
        }

        @Override
        public Application newApplication(ClassLoader cl, String className, Context context)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            return DexOpener.super.newApplication(cl, className, context);
        }

    }

}

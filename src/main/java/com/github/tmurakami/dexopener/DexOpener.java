package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

public class DexOpener extends AndroidJUnitRunner {

    private Installer installer = newInstaller();
    private SuperCalls superCalls = new SuperCalls();

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

    private static Installer newInstaller() {
        return Installer.builder()
                .classNameFilter(new ClassNameFilterImpl())
                .build(new TransformerImpl.FactoryImpl());
    }

    private void init() {
        if (!initialized) {
            initialized = true;
            installer.install(superCalls.getTargetContext());
        }
    }

    class SuperCalls {

        Context getTargetContext() {
            return DexOpener.super.getTargetContext();
        }

        void onCreate(Bundle arguments) {
            DexOpener.super.onCreate(arguments);
        }

        Application newApplication(ClassLoader cl, String className, Context context)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            return DexOpener.super.newApplication(cl, className, context);
        }

    }

}

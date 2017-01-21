package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.logging.Logger;

/**
 * The Dex opener is an object that provides the ability to mock final classes and methods.
 */
public class DexOpener extends AndroidJUnitRunner {

    private boolean initialized;

    @Override
    public void onCreate(Bundle arguments) {
        init();
        super.onCreate(arguments);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // On Android 4.1 or later, this is called before onCreate.
        init();
        return super.newApplication(cl, className, context);
    }

    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        Context context = getTargetContext();
        ApplicationInfo ai = context.getApplicationInfo();
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            deleteFiles(cacheDir.listFiles());
        }
        DexClassSource.Factory dexClassSourceFactory = new DexClassSource.Factory(cacheDir);
        ClassSource source = new ClassSourceImpl(ai.sourceDir, new ClassNameFilter(), dexClassSourceFactory);
        ClassInjector.from(source).into(context.getClassLoader());
    }

    private static void deleteFiles(File[] files) {
        for (File f : files) {
            if (f.isDirectory()) {
                deleteFiles(f.listFiles());
            }
            if (f.exists() && !f.delete()) {
                Logger.getLogger("com.github.tmurakami.dexopener").warning("Cannot delete " + f);
            }
        }
    }

}

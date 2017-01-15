package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import com.github.tmurakami.classinjector.ClassDefiner;
import com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.classinjector.android.DexClassDefiner;

import java.io.File;
import java.util.logging.Logger;

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
        ClassDefiner definer = new DexClassDefiner(cacheDir);
        ClassSource source = new DexClassSource(ai.sourceDir, new ClassNameFilter());
        ClassInjector.using(definer).from(source).into(context.getClassLoader());
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

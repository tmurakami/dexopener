package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.tmurakami.classinjector.ClassInjector;

import java.io.File;
import java.util.logging.Logger;

final class DexOpenerImpl extends DexOpener {

    private final Instrumentation instrumentation;
    private final ClassNameFilter classNameFilter;

    DexOpenerImpl(Instrumentation instrumentation, ClassNameFilter classNameFilter) {
        this.instrumentation = instrumentation;
        this.classNameFilter = classNameFilter;
    }

    @Override
    public void install() {
        Context context = instrumentation.getTargetContext();
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException("An Application instance has already been created");
        }
        ApplicationInfo ai = context.getApplicationInfo();
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            deleteFiles(cacheDir.listFiles());
        }
        DexClassSourceFactory dexClassSourceFactory = new DexClassSourceFactory(cacheDir);
        ClassInjector
                .from(new ClassSourceImpl(ai.sourceDir, classNameFilter, dexClassSourceFactory))
                .into(context.getClassLoader());
    }

    private static void deleteFiles(File[] files) {
        for (File f : files) {
            if (f.isDirectory()) {
                deleteFiles(f.listFiles());
            }
            if (f.exists() && !f.delete()) {
                Logger.getLogger(BuildConfig.APPLICATION_ID).warning("Cannot delete " + f);
            }
        }
    }

}

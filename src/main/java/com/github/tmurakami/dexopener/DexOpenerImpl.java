package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassInjector;

import java.io.File;
import java.util.logging.Logger;

final class DexOpenerImpl extends DexOpener {

    private final ClassNameFilter classNameFilter;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexOpenerImpl(ClassNameFilter classNameFilter,
                  DexFileLoader dexFileLoader,
                  DexClassFileFactory dexClassFileFactory) {
        this.classNameFilter = classNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    @Override
    public void install(@NonNull Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        if (context == null) {
            throw new IllegalStateException("The instrumentation has not been initialized yet");
        }
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException("An Application instance has already been created");
        }
        ApplicationInfo ai = context.getApplicationInfo();
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            deleteFiles(cacheDir.listFiles());
        }
        ClassInjector
                .from(new ClassSourceImpl(
                        ai.sourceDir,
                        classNameFilter,
                        new DexClassSourceFactory(cacheDir, classNameFilter, dexFileLoader, dexClassFileFactory)))
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

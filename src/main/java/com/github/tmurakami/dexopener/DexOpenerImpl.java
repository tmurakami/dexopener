package com.github.tmurakami.dexopener;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassInjector;

import java.io.File;
import java.util.logging.Logger;

final class DexOpenerImpl extends DexOpener {

    private final ApplicationInfo applicationInfo;
    private final ClassNameFilter classNameFilter;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexOpenerImpl(ApplicationInfo applicationInfo,
                  ClassNameFilter classNameFilter,
                  DexFileLoader dexFileLoader,
                  DexClassFileFactory dexClassFileFactory) {
        this.applicationInfo = applicationInfo;
        this.classNameFilter = classNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    @Override
    public void install(@NonNull ClassLoader classLoader) {
        ApplicationInfo ai = applicationInfo;
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            deleteFiles(cacheDir.listFiles());
        }
        ClassInjector
                .from(new AndroidClassSource(
                        ai.sourceDir,
                        classNameFilter,
                        new DexClassSourceFactory(cacheDir, dexFileLoader, dexClassFileFactory)))
                .into(classLoader);
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

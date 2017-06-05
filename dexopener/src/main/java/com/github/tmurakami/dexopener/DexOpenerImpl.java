package com.github.tmurakami.dexopener;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassInjector;

import java.io.File;

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
    public void installTo(@NonNull ClassLoader classLoader) {
        ApplicationInfo ai = applicationInfo;
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        ClassInjector.from(new AndroidClassSource(ai.sourceDir,
                                                  classNameFilter,
                                                  new DexFilesFactory(classNameFilter, cacheDir, dexFileLoader),
                                                  new DexClassSourceFactory(dexClassFileFactory)))
                     .into(classLoader);
    }

}

package com.github.tmurakami.dexopener;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

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
        assertMinSdkVersionIsLowerThan26(ai);
        ClassInjector.from(newClassSource(ai)).into(classLoader);
    }

    private ClassSource newClassSource(ApplicationInfo ai) {
        return new AndroidClassSource(ai.sourceDir,
                                      classNameFilter,
                                      newDexFilesFactory(ai),
                                      new DexClassSourceFactory(dexClassFileFactory));
    }

    private DexFilesFactory newDexFilesFactory(ApplicationInfo ai) {
        return new DexFilesFactory(classNameFilter, getCacheDir(ai), dexFileLoader);
    }

    private static File getCacheDir(ApplicationInfo ai) {
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        return cacheDir;
    }

    // Currently, dexlib2 does not support version `038` of the Dex format.
    private static void assertMinSdkVersionIsLowerThan26(ApplicationInfo ai) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ai.minSdkVersion >= 26) {
            throw new UnsupportedOperationException("minSdkVersion must be lower than 26");
        }
    }

}

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;

final class DexOpenerImpl extends DexOpener {

    private final Context context;
    private final ClassNameFilter classNameFilter;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexOpenerImpl(Context context,
                  ClassNameFilter classNameFilter,
                  DexFileLoader dexFileLoader,
                  DexClassFileFactory dexClassFileFactory) {
        this.context = context;
        this.classNameFilter = classNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    @Override
    public void installTo(@NonNull ClassLoader classLoader) {
        Context context = this.context;
        ApplicationInfo ai = context.getApplicationInfo();
        assertMinSdkVersionIsLowerThan26(ai);
        assertApplicationIsNotCreated(context);
        ClassInjector.from(newClassSource(ai)).into(classLoader);
    }

    private ClassSource newClassSource(ApplicationInfo ai) {
        return new AndroidClassSource(ai.sourceDir,
                                      classNameFilter,
                                      newDexClassSourceFactory(ai));
    }

    private DexClassSourceFactory newDexClassSourceFactory(ApplicationInfo ai) {
        return new DexClassSourceFactory(classNameFilter,
                                         getCacheDir(ai),
                                         dexFileLoader,
                                         dexClassFileFactory);
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

    private static void assertApplicationIsNotCreated(Context context) {
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException("The Application instance has already been created.");
        }
    }

}

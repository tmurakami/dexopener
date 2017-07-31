package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;

final class DexOpenerImpl extends DexOpener {

    private final Context context;
    private final AndroidClassSourceFactory androidClassSourceFactory;
    private final ClassInjectorFactory classInjectorFactory;

    DexOpenerImpl(Context context,
                  AndroidClassSourceFactory androidClassSourceFactory,
                  ClassInjectorFactory classInjectorFactory) {
        this.context = context;
        this.androidClassSourceFactory = androidClassSourceFactory;
        this.classInjectorFactory = classInjectorFactory;
    }

    @Override
    public void installTo(@NonNull ClassLoader target) {
        Context context = this.context;
        ApplicationInfo ai = context.getApplicationInfo();
        assertMinSdkVersionIsLowerThan26(ai);
        assertApplicationIsNotCreated(context);
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        ClassSource classSource = androidClassSourceFactory.newClassSource(ai.sourceDir, cacheDir);
        classInjectorFactory.newClassInjector(classSource).into(target);
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

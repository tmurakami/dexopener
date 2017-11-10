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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && ai.minSdkVersion >= Build.VERSION_CODES.O) {
            // dexlib2 does not currently support version `038` of the DEX format added in the
            // Android O.
            throw new UnsupportedOperationException(
                    "minSdkVersion must be lower than " + Build.VERSION_CODES.O);
        }
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException(
                    "This method must be called before the Application instance is created");
        }
        File cacheDir = new File(getCodeCacheDir(context), "dexopener");
        if (cacheDir.isDirectory()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        ClassSource classSource = androidClassSourceFactory.newClassSource(ai.sourceDir, cacheDir);
        classInjectorFactory.newClassInjector(classSource).into(target);
    }

    private static File getCodeCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getCodeCacheDir();
        }
        String parentName = "code_cache";
        File dir = new File(context.getApplicationInfo().dataDir, parentName);
        return dir.mkdir() || dir.isDirectory() ? dir : new File(context.getFilesDir(), parentName);
    }

}

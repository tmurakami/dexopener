package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
final class DexOpenerImpl extends DexOpener {

    private static final Executor EXECUTOR;

    static {
        final AtomicInteger count = new AtomicInteger();
        int nThreads = Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), 4));
        EXECUTOR = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "DexOpener #" + count.incrementAndGet());
            }
        });
    }

    private final Context context;
    private final ClassNameFilter classNameFilter;
    private final DexFileLoader dexFileLoader;
    private final DexClassSourceFactory dexClassSourceFactory;

    DexOpenerImpl(Context context,
                  ClassNameFilter classNameFilter,
                  DexFileLoader dexFileLoader,
                  DexClassSourceFactory dexClassSourceFactory) {
        this.context = context;
        this.classNameFilter = classNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.dexClassSourceFactory = dexClassSourceFactory;
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
                                      newDexFileHolderMapper(ai),
                                      dexClassSourceFactory);
    }

    private DexFileHolderMapper newDexFileHolderMapper(ApplicationInfo ai) {
        return new DexFileHolderMapper(classNameFilter, EXECUTOR, newDexFileTaskFactory(ai));
    }

    private DexFileTaskFactory newDexFileTaskFactory(ApplicationInfo ai) {
        return new DexFileTaskFactory(getCacheDir(ai), new ClassOpener(), dexFileLoader);
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

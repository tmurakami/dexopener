package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class Installer {

    private final MultiDexHelper multiDexHelper;
    private final DexElementFactory elementFactory;
    private final ClassLoaderFactory classLoaderFactory;
    private final ClassLoaderHelper classLoaderHelper;

    Installer(MultiDexHelper multiDexHelper,
              DexElementFactory elementFactory,
              ClassLoaderFactory classLoaderFactory,
              ClassLoaderHelper classLoaderHelper) {
        this.multiDexHelper = multiDexHelper;
        this.elementFactory = elementFactory;
        this.classLoaderFactory = classLoaderFactory;
        this.classLoaderHelper = classLoaderHelper;
    }

    void install(Context context) {
        List<DexElement> elements = collectDexElements(context);
        ClassLoader classLoader = context.getClassLoader();
        ClassLoader newClassLoader = classLoaderFactory.newClassLoader(classLoader, elements);
        classLoaderHelper.setParent(classLoader, newClassLoader);
    }

    static Builder builder() {
        return new Builder();
    }

    private List<DexElement> collectDexElements(Context context) {
        multiDexHelper.install(context);
        ApplicationInfo ai = context.getApplicationInfo();
        File apk = new File(ai.sourceDir);
        File[] files = getSecondaryZipFiles(ai, apk);
        List<DexElement> elements = new ArrayList<>(files.length + 1);
        File cacheDir = getCacheDir(ai.dataDir);
        elements.add(elementFactory.newDexElement(apk, cacheDir));
        for (File f : files) {
            elements.add(elementFactory.newDexElement(f, cacheDir));
        }
        return elements;
    }

    private static File[] getSecondaryZipFiles(ApplicationInfo ai, File apk) {
        File dir = new File(ai.dataDir, "code_cache/secondary-dexes");
        final String prefix = apk.getName() + ".classes";
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName();
                return f.isFile() && name.startsWith(prefix) && name.endsWith(".zip");
            }
        });
        return files == null ? new File[0] : files;
    }

    private static File getCacheDir(String dataDir) {
        File cacheDir = new File(dataDir, "code_cache/dexopener");
        if (cacheDir.isDirectory()) {
            IOUtils.deleteFiles(cacheDir.listFiles());
        } else if (!cacheDir.mkdirs()) {
            throw new Error("Cannot create " + cacheDir);
        }
        return cacheDir;
    }

    static final class Builder {

        private static final ExecutorService EXECUTOR_SERVICE;

        static {
            int processors = Runtime.getRuntime().availableProcessors();
            int poolSize = Math.max(1, Math.min(processors - 1, 4));
            final AtomicInteger count = new AtomicInteger();
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    poolSize,
                    poolSize * 2 + 1,
                    30L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(32),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(@NonNull Runnable r) {
                            return new Thread(r, "DexOpener #" + count.incrementAndGet());
                        }
                    });
            executor.allowCoreThreadTimeOut(true);
            EXECUTOR_SERVICE = executor;
        }

        private ClassNameFilter classNameFilter = new ClassNameFilter() {
            @Override
            public boolean accept(String name) {
                return true;
            }
        };

        Builder() {
        }

        Builder classNameFilter(ClassNameFilter classNameFilter) {
            this.classNameFilter = classNameFilter;
            return this;
        }

        Installer build(Transformer.Factory transformerFactory) {
            ClassNameFilter classNameFilter = new BuiltinClassNameFilter(this.classNameFilter);
            ClassNameReader classNameReader = new ClassNameReader(classNameFilter);
            DexFileHelper dexFileHelper = new DexFileHelper();
            DexElementFactory elementFactory = new DexElementFactory(dexFileHelper, classNameReader, transformerFactory, EXECUTOR_SERVICE);
            ClassLoaderFactory classLoaderFactory = new ClassLoaderFactory(classNameFilter);
            return new Installer(new MultiDexHelper(), elementFactory, classLoaderFactory, new ClassLoaderHelper());
        }

    }

}

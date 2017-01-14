package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class Installer {

    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        int corePoolSize = Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), 4));
        final AtomicInteger count = new AtomicInteger();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(16),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        return new Thread(r, "DexOpener #" + count.incrementAndGet());
                    }
                });
        executor.allowCoreThreadTimeOut(true);
        EXECUTOR_SERVICE = executor;
    }

    private final DexElementFactory elementFactory;
    private final ClassLoaderFactory classLoaderFactory;
    private final ClassLoaderHelper classLoaderHelper;

    private Installer(DexElementFactory elementFactory,
                      ClassLoaderFactory classLoaderFactory,
                      ClassLoaderHelper classLoaderHelper) {
        this.elementFactory = elementFactory;
        this.classLoaderFactory = classLoaderFactory;
        this.classLoaderHelper = classLoaderHelper;
    }

    void install(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        File cacheDir = getCacheDir(ai.dataDir);
        Iterable<DexElement> elements = toDexElements(elementFactory, ai.sourceDir, cacheDir);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, elements));
    }

    static Installer create() {
        ClassNameFilter classNameFilter = new ClassNameFilter();
        DexFileLoader fileLoader = new DexFileLoader();
        ClassNameReader classNameReader = new ClassNameReader(classNameFilter);
        DexFileGenerator fileGenerator = new DexFileGenerator(fileLoader);
        DexElementFactory elementFactory = new DexElementFactory(classNameReader, fileGenerator, EXECUTOR_SERVICE);
        ClassLoaderFactory classLoaderFactory = new ClassLoaderFactory(classNameFilter);
        return new Installer(elementFactory, classLoaderFactory, new ClassLoaderHelper());
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

    private static Iterable<DexElement> toDexElements(DexElementFactory elementFactory,
                                                      String sourceDir,
                                                      File cacheDir) {
        List<DexElement> elements = new ArrayList<>();
        ZipInputStream in = null;
        try {
            in = new ZipInputStream(new FileInputStream(sourceDir));
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    elements.add(elementFactory.newDexElement(IOUtils.readBytes(in), cacheDir));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        if (elements.isEmpty()) {
            throw new Error(sourceDir + " does not contain the classes.dex");
        }
        return elements;
    }

}

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

final class DexOpenerHelperImpl implements DexOpenerHelper {

    private static final Field M_BASE;

    static {
        try {
            M_BASE = ContextWrapper.class.getDeclaredField("mBase");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    private final ClassesJarGenerator classesJarGenerator;
    private final ClassLoaderFactory classLoaderFactory;

    DexOpenerHelperImpl(ClassesJarGenerator classesJarGenerator,
                        ClassLoaderFactory classLoaderFactory) {
        this.classesJarGenerator = classesJarGenerator;
        this.classLoaderFactory = classLoaderFactory;
    }

    @Override
    public void setBaseContext(ContextWrapper context, Context base) {
        M_BASE.setAccessible(true);
        try {
            M_BASE.set(context, base);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setContextClassLoader(Thread thread, ClassLoader classLoader) {
        thread.setContextClassLoader(classLoader);
    }

    @Override
    public ClassLoader newClassLoader(String apkPath,
                                      String testApkPath,
                                      File cacheDir,
                                      ClassLoader parent) throws IOException {
        List<String> dexPaths = new ArrayList<>(2);
        dexPaths.add(classesJarGenerator.generateClassesJar(apkPath, cacheDir));
        if (!testApkPath.equals(apkPath)) {
            dexPaths.add(testApkPath);
        }
        return classLoaderFactory.newClassLoader(dexPaths, cacheDir.getCanonicalPath(), parent);
    }

}

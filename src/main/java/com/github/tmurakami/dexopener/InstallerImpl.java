package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;

final class InstallerImpl extends Installer {

    private final DexElementFactory elementFactory;
    private final ClassLoaderFactory classLoaderFactory;
    private final ClassLoaderHelper classLoaderHelper;

    InstallerImpl(DexElementFactory elementFactory,
                  ClassLoaderFactory classLoaderFactory,
                  ClassLoaderHelper classLoaderHelper) {
        this.elementFactory = elementFactory;
        this.classLoaderFactory = classLoaderFactory;
        this.classLoaderHelper = classLoaderHelper;
    }

    @Override
    public void install(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        File cacheDir = new File(ai.dataDir, "code_cache/dexopener");
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new Error("Cannot create " + cacheDir);
        }
        IOUtils.deleteFiles(cacheDir.listFiles());
        DexElement element = elementFactory.newDexElement(new File(ai.sourceDir), cacheDir);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, element));
    }

}

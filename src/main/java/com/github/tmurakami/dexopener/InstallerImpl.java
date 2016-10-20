package com.github.tmurakami.dexopener;

import android.content.Context;

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
        File file = new File(context.getApplicationInfo().sourceDir);
        File cacheDir = context.getDir("dexopener", Context.MODE_PRIVATE);
        DexElement element = elementFactory.newDexElement(file, cacheDir);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, element));
    }

}

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

final class InstallerImpl extends Installer {

    private final MultiDexHelper multiDexHelper;
    private final DexElementFactory elementFactory;
    private final ClassLoaderFactory classLoaderFactory;
    private final ClassLoaderHelper classLoaderHelper;

    InstallerImpl(MultiDexHelper multiDexHelper,
                  DexElementFactory elementFactory,
                  ClassLoaderFactory classLoaderFactory,
                  ClassLoaderHelper classLoaderHelper) {
        this.multiDexHelper = multiDexHelper;
        this.elementFactory = elementFactory;
        this.classLoaderFactory = classLoaderFactory;
        this.classLoaderHelper = classLoaderHelper;
    }

    @Override
    public void install(Context context) {
        multiDexHelper.installMultiDex(context);
        List<DexElement> elements = collectDexElements(context);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, elements));
    }

    private List<DexElement> collectDexElements(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        File apk = new File(ai.sourceDir);
        File[] files = getSecondaryZipFiles(ai, apk);
        List<DexElement> elements = new ArrayList<>(files.length + 1);
        File cacheDir = context.getDir("dexopener", Context.MODE_PRIVATE);
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

}

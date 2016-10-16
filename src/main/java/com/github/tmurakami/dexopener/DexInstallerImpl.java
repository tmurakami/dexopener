package com.github.tmurakami.dexopener;


import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

final class DexInstallerImpl extends DexInstaller {

    private final MultiDexHelper multiDexHelper;
    private final DexFactory dexFactory;
    private final ClassLoaderFactory classLoaderFactory;
    private final ClassLoaderHelper classLoaderHelper;

    DexInstallerImpl(MultiDexHelper multiDexHelper,
                     DexFactory dexFactory,
                     ClassLoaderFactory classLoaderFactory,
                     ClassLoaderHelper classLoaderHelper) {
        this.multiDexHelper = multiDexHelper;
        this.dexFactory = dexFactory;
        this.classLoaderFactory = classLoaderFactory;
        this.classLoaderHelper = classLoaderHelper;
    }

    @Override
    public void install(Context context) {
        multiDexHelper.installMultiDex(context);
        List<Dex> dexes = collectDexes(context);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, dexes));
    }

    private List<Dex> collectDexes(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        File apk = new File(ai.sourceDir);
        File[] files = getSecondaryDexes(ai, apk);
        List<Dex> dexes = new ArrayList<>(files.length + 1);
        File cacheDir = context.getDir("dexopener", Context.MODE_PRIVATE);
        dexes.add(dexFactory.newDex(apk, cacheDir));
        for (File f : files) {
            dexes.add(dexFactory.newDex(f, cacheDir));
        }
        return dexes;
    }

    private static File[] getSecondaryDexes(ApplicationInfo ai, File apk) {
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

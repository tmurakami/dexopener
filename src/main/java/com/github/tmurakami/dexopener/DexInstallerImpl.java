package com.github.tmurakami.dexopener;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.File;
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
        List<Dex> dices = collectDices(context);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, dices));
    }

    private List<Dex> collectDices(Context context) {
        int total = context.getSharedPreferences("multidex.version", getMode()).getInt("dex.number", 1);
        List<Dex> dices = new ArrayList<>(total);
        ApplicationInfo ai = context.getApplicationInfo();
        File apk = new File(ai.sourceDir);
        File cacheDir = context.getDir("dexopener", Context.MODE_PRIVATE);
        dices.add(dexFactory.newDex(apk, cacheDir));
        File dexDir = new File(ai.dataDir, "code_cache/secondary-dexes");
        String prefix = apk.getName() + ".classes";
        for (int i = 2; i <= total; i++) {
            File file = new File(dexDir, prefix + i + ".zip");
            dices.add(dexFactory.newDex(file, cacheDir));
        }
        return dices;
    }

    @SuppressWarnings("deprecation")
    private static int getMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Context.MODE_MULTI_PROCESS;
        } else {
            return Context.MODE_PRIVATE;
        }
    }

}

package com.github.tmurakami.dexopener;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexFile;

final class DexInstallerImpl extends DexInstaller {

    private final MultiDex multiDex;
    private final DexConverter dexConverter;
    private final DexFileLoader dexFileLoader;
    private final ClassLoaderInstaller classLoaderInstaller;

    DexInstallerImpl(MultiDex multiDex,
                     DexConverter dexConverter,
                     DexFileLoader dexFileLoader,
                     ClassLoaderInstaller classLoaderInstaller) {
        this.multiDex = multiDex;
        this.dexConverter = dexConverter;
        this.dexFileLoader = dexFileLoader;
        this.classLoaderInstaller = classLoaderInstaller;
    }

    @Override
    public void install(Context context) throws IOException {
        multiDex.install(context);
        File cacheDir = context.getDir("dexopener", Context.MODE_PRIVATE);
        List<DexFile> dexFiles = collectDexFiles(context, cacheDir);
        classLoaderInstaller.install(context.getClassLoader(), dexFiles);
    }

    private List<DexFile> collectDexFiles(Context context, File cacheDir) throws IOException {
        int total = getTotalDexFiles(context);
        List<DexFile> dexFiles = new ArrayList<>(total);
        ApplicationInfo ai = context.getApplicationInfo();
        File apk = new File(ai.sourceDir);
        addDexFile(apk, cacheDir, dexFiles);
        File dexDir = new File(ai.dataDir, "code_cache/secondary-dexes");
        String prefix = apk.getName() + ".classes";
        for (int i = 2; i <= total; i++) {
            File file = new File(dexDir, prefix + i + ".zip");
            addDexFile(file, cacheDir, dexFiles);
        }
        return dexFiles;
    }

    private void addDexFile(File zip, File cacheDir, List<DexFile> dexFiles) throws IOException {
        File f = dexConverter.convert(zip, cacheDir);
        String outputPathName = new File(cacheDir, f.getName() + ".dex").getCanonicalPath();
        dexFiles.add(dexFileLoader.load(f.getCanonicalPath(), outputPathName));
    }

    @SuppressWarnings("deprecation")
    private static int getTotalDexFiles(Context context) {
        int mode = Context.MODE_PRIVATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mode |= Context.MODE_MULTI_PROCESS;
        }
        return context.getSharedPreferences("multidex.version", mode).getInt("dex.number", 1);
    }

}

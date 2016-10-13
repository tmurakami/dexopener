package com.github.tmurakami.dexopener;

import android.content.Context;

import java.io.IOException;

import dalvik.system.DexFile;

abstract class DexInstaller {

    abstract void install(Context context) throws IOException;

    static DexInstaller create() {
        return new DexInstallerImpl(new MultiDexImpl(), new DexConverterImpl(new ClassNameFilterImpl()), newDexFileLoader(), new ClassLoaderInstallerImpl());
    }

    private static DexFileLoader newDexFileLoader() {
        return new DexFileLoader() {
            @Override
            public DexFile load(String sourcePathName, String outputPathName) throws IOException {
                return DexFile.loadDex(sourcePathName, outputPathName, 0);
            }
        };
    }

}

package com.github.tmurakami.classinjector.android;

import java.io.IOException;

import dalvik.system.DexFile;

final class DexFileLoader {

    static final DexFileLoader INSTANCE = new DexFileLoader();

    private DexFileLoader() {
    }

    DexFile loadDex(String sourcePathName, String outputPathName, int flags) throws IOException {
        return DexFile.loadDex(sourcePathName, outputPathName, flags);
    }

}

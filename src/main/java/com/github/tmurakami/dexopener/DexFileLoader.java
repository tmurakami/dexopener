package com.github.tmurakami.dexopener;

import java.io.IOException;

import dalvik.system.DexFile;

final class DexFileLoader {
    DexFile load(String sourcePathName, String outputPathName) throws IOException {
        return DexFile.loadDex(sourcePathName, outputPathName, 0);
    }
}

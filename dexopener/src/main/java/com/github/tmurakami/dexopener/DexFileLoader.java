package com.github.tmurakami.dexopener;

import java.io.IOException;

final class DexFileLoader {
    @SuppressWarnings("deprecation")
    dalvik.system.DexFile loadDex(String sourcePathName, String outputPathName) throws IOException {
        return dalvik.system.DexFile.loadDex(sourcePathName, outputPathName, 0);
    }
}

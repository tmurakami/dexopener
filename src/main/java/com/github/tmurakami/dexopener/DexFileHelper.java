package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexFile;

final class DexFileHelper {

    DexFile newDexFile(File file) throws IOException {
        return new DexFile(file);
    }

    DexFile loadDexFile(String sourcePathName, String outputPathName) throws IOException {
        return DexFile.loadDex(sourcePathName, outputPathName, 0);
    }

}

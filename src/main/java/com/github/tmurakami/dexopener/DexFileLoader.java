package com.github.tmurakami.dexopener;

import java.io.IOException;

import dalvik.system.DexFile;

interface DexFileLoader {
    DexFile load(String sourcePathName, String outputPathName) throws IOException;
}

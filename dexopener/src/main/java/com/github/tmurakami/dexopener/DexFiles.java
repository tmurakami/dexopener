package com.github.tmurakami.dexopener;

import java.io.IOException;

import dalvik.system.DexFile;

interface DexFiles {
    DexFile get(String className) throws IOException;
}

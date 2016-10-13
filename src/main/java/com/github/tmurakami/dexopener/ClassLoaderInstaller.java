package com.github.tmurakami.dexopener;

import java.util.List;

import dalvik.system.DexFile;

interface ClassLoaderInstaller {
    void install(ClassLoader classLoader, List<DexFile> dexFiles);
}

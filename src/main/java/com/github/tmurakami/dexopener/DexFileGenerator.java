package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexFile;

interface DexFileGenerator {
    DexFile generateDexFile(ApplicationReader ar, File cacheDir, String... classesToVisit) throws IOException;
}

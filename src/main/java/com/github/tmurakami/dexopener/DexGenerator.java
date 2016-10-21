package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;

interface DexGenerator {
    File generateDexFile(ApplicationReader ar, File cacheDir, String... classesToVisit) throws IOException;
}

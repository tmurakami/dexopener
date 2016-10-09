package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;

interface ClassesJarGenerator {
    String generateClassesJar(String apkPath, File cacheDir) throws IOException;
}

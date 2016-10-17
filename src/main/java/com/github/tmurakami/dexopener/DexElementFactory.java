package com.github.tmurakami.dexopener;

import java.io.File;

interface DexElementFactory {
    DexElement newDexElement(File file, File cacheDir);
}

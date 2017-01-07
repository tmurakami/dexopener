package com.github.tmurakami.dexopener;

import java.io.File;

interface DexElementFactory {
    DexElement newDexElement(byte[] bytes, File cacheDir);
}

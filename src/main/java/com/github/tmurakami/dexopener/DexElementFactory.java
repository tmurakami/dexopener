package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;

interface DexElementFactory {
    DexElement newDexElement(byte[] bytes, File cacheDir) throws IOException;
}

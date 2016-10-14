package com.github.tmurakami.dexopener;

import java.io.File;

interface DexFactory {
    Dex newDex(File file, File cacheDir);
}

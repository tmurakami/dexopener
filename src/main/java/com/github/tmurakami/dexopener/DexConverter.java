package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;

interface DexConverter {
    File convert(File zip, File cacheDir) throws IOException;
}

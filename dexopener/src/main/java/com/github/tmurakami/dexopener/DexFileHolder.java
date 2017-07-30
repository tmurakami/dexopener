package com.github.tmurakami.dexopener;

import java.io.IOException;

interface DexFileHolder {
    dalvik.system.DexFile get() throws IOException;
}

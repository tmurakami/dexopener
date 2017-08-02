package com.github.tmurakami.dexopener;

import java.io.IOException;

interface DexFileHolder {
    @SuppressWarnings("deprecation")
    dalvik.system.DexFile get() throws IOException;
}

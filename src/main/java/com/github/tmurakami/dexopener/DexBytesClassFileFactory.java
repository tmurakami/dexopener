package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.android.DexBytesClassFile;

import java.io.File;

final class DexBytesClassFileFactory {

    static final DexBytesClassFileFactory INSTANCE = new DexBytesClassFileFactory();

    private DexBytesClassFileFactory() {
    }

    ClassFile create(String className, byte[] bytes, File cacheDir) {
        return new DexBytesClassFile(className, bytes, cacheDir);
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.android.DexClassFile;

import dalvik.system.DexFile;

final class DexClassFileFactory {

    static final DexClassFileFactory INSTANCE = new DexClassFileFactory();

    private DexClassFileFactory() {
    }

    ClassFile newClassFile(String className, DexFile dexFile) {
        return new DexClassFile(className, dexFile);
    }

}

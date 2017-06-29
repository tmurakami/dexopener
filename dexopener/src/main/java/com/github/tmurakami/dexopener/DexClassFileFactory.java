package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.android.DexClassFile;

final class DexClassFileFactory {
    @SuppressWarnings("deprecation")
    ClassFile newClassFile(String className, dalvik.system.DexFile dexFile) {
        return new DexClassFile(className, dexFile);
    }
}

package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;

import java.io.IOException;

import dalvik.system.DexFile;

final class DexClassSource implements ClassSource {

    private final DexFiles dexFiles;
    private final DexClassFileFactory classFileFactory;

    DexClassSource(DexFiles dexFiles, DexClassFileFactory classFileFactory) {
        this.dexFiles = dexFiles;
        this.classFileFactory = classFileFactory;
    }

    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        DexFile dexFile = dexFiles.get(className);
        return dexFile == null ? null : classFileFactory.newClassFile(className, dexFile);
    }

}

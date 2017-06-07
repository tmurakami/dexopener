package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

final class DexClassSourceFactory {

    private final DexClassFileFactory dexClassFileFactory;

    DexClassSourceFactory(DexClassFileFactory dexClassFileFactory) {
        this.dexClassFileFactory = dexClassFileFactory;
    }

    ClassSource newClassSource(DexFiles dexFiles) {
        return new DexClassSource(dexFiles, dexClassFileFactory);
    }

}

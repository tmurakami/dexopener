package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.util.Map;

final class DexClassSourceFactory {

    private final DexClassFileFactory dexClassFileFactory;

    DexClassSourceFactory(DexClassFileFactory dexClassFileFactory) {
        this.dexClassFileFactory = dexClassFileFactory;
    }

    ClassSource newClassSource(Map<String, DexFileHolder> holderMap) {
        return new DexClassSource(holderMap, dexClassFileFactory);
    }

}

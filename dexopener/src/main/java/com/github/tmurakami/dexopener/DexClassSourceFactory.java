package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.util.Map;

final class DexClassSourceFactory {
    ClassSource newClassSource(Map<String, DexFileHolder> holderMap) {
        return new DexClassSource(holderMap, new DexClassFileFactory());
    }
}

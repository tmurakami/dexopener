package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.IOException;
import java.util.Map;

final class DexClassSource implements ClassSource {

    private final Map<String, DexFileHolder> holderMap;
    private final DexClassFileFactory dexClassFileFactory;

    DexClassSource(Map<String, DexFileHolder> holderMap,
                   DexClassFileFactory dexClassFileFactory) {
        this.holderMap = holderMap;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        DexFileHolder holder = holderMap.get(className);
        return holder == null ? null : dexClassFileFactory.newClassFile(className, holder.get());
    }

}

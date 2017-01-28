package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_CODE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_DEBUG;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSourceFactory {

    private final File cacheDir;
    private final ClassNameFilter classNameFilter;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory dexClassFileFactory;

    DexClassSourceFactory(File cacheDir,
                          ClassNameFilter classNameFilter,
                          DexFileLoader dexFileLoader,
                          DexClassFileFactory dexClassFileFactory) {
        this.cacheDir = cacheDir;
        this.classNameFilter = classNameFilter;
        this.dexFileLoader = dexFileLoader;
        this.dexClassFileFactory = dexClassFileFactory;
    }

    ClassSource newClassSource(byte[] bytes) {
        ApplicationReader ar = new ApplicationReader(ASM4, bytes);
        ClassNameReader r = new ClassNameReader(classNameFilter);
        ar.accept(r, null, SKIP_CODE | SKIP_DEBUG);
        return new DexClassSource(ar, r.getClassNames(), cacheDir, dexFileLoader, dexClassFileFactory);
    }

}

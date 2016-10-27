package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexElementFactoryImpl implements DexElementFactory {

    private final ClassNameReader classNameReader;
    private final DexFileGenerator fileGenerator;

    DexElementFactoryImpl(ClassNameReader classNameReader, DexFileGenerator fileGenerator) {
        this.classNameReader = classNameReader;
        this.fileGenerator = fileGenerator;
    }

    @Override
    public DexElement newDexElement(byte[] bytes, File cacheDir) throws IOException {
        ApplicationReader ar = new ApplicationReader(ASM4, bytes);
        Set<String> classNames = classNameReader.readClassNames((DexFileReader) ar.getDexFile());
        return new DexElementImpl(ar, classNames, cacheDir, fileGenerator);
    }

}

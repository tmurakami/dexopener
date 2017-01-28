package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSourceFactory {
    private final File cacheDir;

    DexClassSourceFactory(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    ClassSource newClassSource(byte[] bytes) {
        ApplicationReader ar = new ApplicationReader(ASM4, bytes);
        Set<String> classNames = readClassNames((DexFileReader) ar.getDexFile());
        return new DexClassSource(ar, classNames, cacheDir, DexFileLoader.INSTANCE, DexClassFileFactory.INSTANCE);
    }

    private static Set<String> readClassNames(DexFileReader reader) {
        int index;
        int classDefinitionsSize = reader.getClassDefinitionsSize();
        Set<String> classNames = new HashSet<>();
        for (index = 0; index < classDefinitionsSize; ++index) {
            reader.seek(reader.getClassDefinitionOffset(index));
            String name = reader.getStringItemFromTypeIndex(reader.uint());
            classNames.add(name.substring(1, name.length() - 1).replace('/', '.'));
        }
        return Collections.unmodifiableSet(classNames);
    }

}

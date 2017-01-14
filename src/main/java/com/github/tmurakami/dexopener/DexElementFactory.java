package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexElementFactory {

    private final ClassNameReader classNameReader;
    private final DexFileGenerator fileGenerator;
    private final ExecutorService executorService;

    DexElementFactory(ClassNameReader classNameReader,
                      DexFileGenerator fileGenerator,
                      ExecutorService executorService) {
        this.classNameReader = classNameReader;
        this.fileGenerator = fileGenerator;
        this.executorService = executorService;
    }

    DexElement newDexElement(byte[] bytes, File cacheDir) {
        ApplicationReader ar = new ApplicationReader(ASM4, bytes);
        Set<Set<String>> classNamesSet = classNameReader.read((DexFileReader) ar.getDexFile());
        ConcurrentMap<Set<String>, DexFile> dexFileMap = new ConcurrentHashMap<>();
        executorService.submit(new DexFileGeneratorTask(ar, cacheDir, fileGenerator, classNamesSet, dexFileMap));
        return new DexElement(new ApplicationReader(ASM4, bytes), cacheDir, fileGenerator, classNamesSet, dexFileMap);
    }

}

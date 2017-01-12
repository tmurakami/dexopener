package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

final class DexElementFactory {

    private final DexFileHelper dexFileHelper;
    private final ClassNameReader classNameReader;
    private final Transformer.Factory transformerFactory;
    private final ExecutorService executorService;

    DexElementFactory(DexFileHelper dexFileHelper,
                      ClassNameReader classNameReader,
                      Transformer.Factory transformerFactory,
                      ExecutorService executorService) {
        this.dexFileHelper = dexFileHelper;
        this.classNameReader = classNameReader;
        this.transformerFactory = transformerFactory;
        this.executorService = executorService;
    }

    DexElement newDexElement(File file, File cacheDir) {
        DexFile dexFile;
        try {
            dexFile = dexFileHelper.newDexFile(file);
        } catch (IOException e) {
            throw new Error(e);
        }
        byte[] dexBytes = readClassesDex(file);
        Iterable<Set<String>> classNamesSet = classNameReader.read(dexFile);
        ConcurrentMap<Set<String>, DexFile> dexFileMap = new ConcurrentHashMap<>();
        executorService.submit(new DexFileGeneratorTask(newDexFileGenerator(dexBytes, cacheDir), classNamesSet, dexFileMap));
        return new DexElement(newDexFileGenerator(dexBytes, cacheDir), classNamesSet, dexFileMap);
    }

    private DexFileGenerator newDexFileGenerator(byte[] dexBytes, File cacheDir) {
        return new DexFileGenerator(transformerFactory.newTransformer(dexBytes), cacheDir, dexFileHelper);
    }

    private static byte[] readClassesDex(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry e = zipFile.getEntry("classes.dex");
            if (e == null) {
                throw new Error(file + " does not contain the classes.dex");
            }
            return IOUtils.readBytes(zipFile.getInputStream(e));
        } catch (IOException e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
    }

}

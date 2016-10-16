package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexFactoryImpl implements DexFactory {

    private final DexFileLoader fileLoader;

    DexFactoryImpl(DexFileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    @Override
    public Dex newDex(File file, File cacheDir) {
        return new DexImpl(newApplicationReader(file), cacheDir, fileLoader);
    }

    private static ApplicationReader newApplicationReader(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry e = zipFile.getEntry("classes.dex");
            if (e == null) {
                throw new Error(file + " does not contain the classes.dex");
            }
            return new ApplicationReader(ASM4, zipFile.getInputStream(e));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(zipFile);
        }
    }

    private static void closeQuietly(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {
            }
        }
    }

}

package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

final class DexFileGenerator {

    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    DexFileGenerator(File cacheDir, DexFileLoader dexFileLoader) {
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    DexFile generateDex(byte[] bytecode) throws IOException {
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
        File zip = File.createTempFile("classes", ".zip", cacheDir);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(bytecode);
            } finally {
                out.close();
            }
            String sourcePathName = zip.getCanonicalPath();
            String outputPathName = sourcePathName + ".dex";
            DexFile dexFile = dexFileLoader.loadDex(sourcePathName, outputPathName, 0);
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("An optimized DEX file was generated: " + outputPathName);
            }
            return dexFile;
        } finally {
            FileUtils.delete(zip);
        }
    }

}

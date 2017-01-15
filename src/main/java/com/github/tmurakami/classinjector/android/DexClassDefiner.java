package com.github.tmurakami.classinjector.android;

import com.github.tmurakami.classinjector.ClassDefiner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

public final class DexClassDefiner implements ClassDefiner {

    private final DexFileLoader dexFileLoader;
    private final File cacheDir;

    public DexClassDefiner(File cacheDir) {
        this(new DexFileLoader(), cacheDir);
    }

    DexClassDefiner(DexFileLoader dexFileLoader, File cacheDir) {
        if (cacheDir == null) {
            throw new IllegalArgumentException("'cacheDir' is null");
        }
        this.dexFileLoader = dexFileLoader;
        this.cacheDir = cacheDir;
    }

    @Override
    public Class defineClass(String name, byte[] bytecode, ClassLoader classLoader) {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
        File zip = null;
        try {
            zip = File.createTempFile("classes", ".zip", cacheDir);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(bytecode);
            } finally {
                out.close();
            }
            String sourcePathName = zip.getCanonicalPath();
            DexFile dexFile = dexFileLoader.loadDex(sourcePathName, sourcePathName + ".dex", 0);
            return dexFile.loadClass(name, classLoader);
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            if (zip != null && !zip.delete()) {
                Logger.getLogger("com.github.tmurakami.classinjector").warning("Cannot delete " + zip);
            }
        }
    }

    static final class DexFileLoader {
        DexFile loadDex(String sourcePathName, String outputPathName, int flags) throws IOException {
            return DexFile.loadDex(sourcePathName, outputPathName, flags);
        }
    }

}

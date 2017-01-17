package com.github.tmurakami.classinjector.android;

import com.github.tmurakami.classinjector.ClassFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

/**
 * A {@link ClassFile} for Android environment.
 */
public final class DexBytesClassFile implements ClassFile {

    private final File[] files = new File[2];
    private final String className;
    private final byte[] bytes;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;

    /**
     * Create an instance.
     *
     * @param className The class name
     * @param bytes     The dex bytes that make up the class for the given class name
     * @param cacheDir  The directory to use for generating a dex file
     */
    public DexBytesClassFile(String className, byte[] bytes, File cacheDir) {
        this(className, bytes, cacheDir, DexFileLoader.INSTANCE);
    }

    DexBytesClassFile(String className, byte[] bytes, File cacheDir, DexFileLoader dexFileLoader) {
        if (className == null) {
            throw new IllegalArgumentException("'className' is null");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("'bytes' is null");
        }
        if (cacheDir == null) {
            throw new IllegalArgumentException("'cacheDir' is null");
        }
        this.className = className;
        this.bytes = copyBytes(bytes, 0, bytes.length);
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
    }

    @Override
    public Class toClass(ClassLoader classLoader) throws IOException {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
        files[0] = File.createTempFile("classes", ".zip", cacheDir);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(files[0]));
        try {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(bytes);
        } finally {
            out.close();
        }
        String sourcePathName = files[0].getCanonicalPath();
        files[1] = new File(sourcePathName + ".dex");
        DexFile dexFile = dexFileLoader.loadDex(sourcePathName, files[1].getCanonicalPath(), 0);
        return dexFile.loadClass(className, classLoader);
    }

    @Override
    public void close() throws IOException {
        for (File f : files) {
            if (f != null && f.exists() && !f.delete()) {
                Logger.getLogger("com.github.tmurakami.classinjector.android").warning("Cannot delete " + f);
            }
        }
    }

    private static byte[] copyBytes(byte[] src, int offset, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(src, offset, bytes, 0, length);
        return bytes;
    }

}

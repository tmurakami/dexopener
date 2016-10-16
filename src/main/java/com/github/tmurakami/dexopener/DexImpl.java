package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

final class DexImpl implements Dex {

    private final ApplicationReader ar;
    private final File cacheDir;
    private final DexFileLoader fileLoader;

    DexImpl(ApplicationReader ar, File cacheDir, DexFileLoader fileLoader) {
        this.ar = ar;
        this.cacheDir = cacheDir;
        this.fileLoader = fileLoader;
    }

    @Override
    public Class loadClass(String name, ClassLoader classLoader) {
        String internalName = 'L' + name.replace('.', '/') + ';';
        if (!hasClassDefinition(ar, internalName)) {
            return null;
        }
        ApplicationWriter aw = new ApplicationWriter();
        ar.accept(new InternalApplicationVisitor(aw), new String[]{internalName}, 0);
        File zip = null;
        File dex = null;
        DexFile dexFile = null;
        try {
            zip = File.createTempFile("classes", ".zip", cacheDir);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(aw.toByteArray());
            } finally {
                closeQuietly(out);
            }
            dex = new File(cacheDir, zip.getName() + ".dex");
            dexFile = fileLoader.load(zip.getCanonicalPath(), dex.getCanonicalPath());
            return dexFile.loadClass(name, classLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(dexFile);
            deleteFiles(zip, dex);
        }
    }

    private static boolean hasClassDefinition(ApplicationReader ar, String internalName) {
        DexFileReader r = (DexFileReader) ar.getDexFile();
        int size = r.getClassDefinitionsSize();
        for (int i = 0; i < size; ++i) {
            r.seek(r.getClassDefinitionOffset(i));
            if (r.getStringItemFromTypeIndex(r.uint()).equals(internalName)) {
                return true;
            }
        }
        return false;
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void closeQuietly(DexFile dexFile) {
        if (dexFile != null) {
            try {
                dexFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void deleteFiles(File... files) {
        for (File f : files) {
            if (f != null && f.exists() && !f.delete()) {
                Logger.getLogger("com.github.tmurakami.dexopener").warning("Cannot delete " + f);
            }
        }
    }

}

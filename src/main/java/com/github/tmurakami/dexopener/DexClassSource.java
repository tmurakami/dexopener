package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSource implements ClassSource {

    private final ApplicationReader applicationReader;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory classFileFactory;

    DexClassSource(ApplicationReader applicationReader,
                   File cacheDir,
                   DexFileLoader dexFileLoader,
                   DexClassFileFactory classFileFactory) {
        this.applicationReader = applicationReader;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.classFileFactory = classFileFactory;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        ApplicationWriter aw = new ApplicationWriter();
        String[] classesToVisit = {'L' + className.replace('.', '/') + ';'};
        applicationReader.accept(new ApplicationOpener(aw), classesToVisit, 0);
        byte[] bytes = aw.toByteArray();
        if (bytes == null) {
            return null;
        }
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
        File zip = File.createTempFile("classes", ".zip", cacheDir);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(bytes);
            } finally {
                out.close();
            }
            String sourcePathName = zip.getCanonicalPath();
            File dex = new File(sourcePathName + ".dex");
            DexFile dexFile = dexFileLoader.loadDex(sourcePathName, dex.getCanonicalPath(), 0);
            return classFileFactory.create(className, dexFile);
        } finally {
            if (zip.exists() && !zip.delete()) {
                Logger.getLogger(BuildConfig.APPLICATION_ID).warning("Cannot delete " + zip);
            }
        }
    }

    static final class Factory {

        private final File cacheDir;

        Factory(File cacheDir) {
            this.cacheDir = cacheDir;
        }

        ClassSource create(byte[] bytes) {
            return new DexClassSource(new ApplicationReader(ASM4, bytes), cacheDir, DexFileLoader.INSTANCE, DexClassFileFactory.INSTANCE);
        }

    }

}

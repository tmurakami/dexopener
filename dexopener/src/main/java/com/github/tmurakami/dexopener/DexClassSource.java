package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSource implements ClassSource {

    private final byte[] byteCode;
    private final Set<String> classNames;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory classFileFactory;

    DexClassSource(byte[] byteCode,
                   Set<String> classNames,
                   File cacheDir,
                   DexFileLoader dexFileLoader,
                   DexClassFileFactory classFileFactory) {
        this.byteCode = byteCode;
        this.classNames = classNames;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.classFileFactory = classFileFactory;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        if (!classNames.contains(className)) {
            return null;
        }
        ApplicationReader ar = new ApplicationReader(ASM4, byteCode);
        ApplicationWriter aw = new ApplicationWriter();
        ApplicationOpener opener = new ApplicationOpener(aw);
        String[] classesToVisit = {'L' + className.replace('.', '/') + ';'};
        try {
            ar.accept(opener, classesToVisit, 0);
        } catch (Exception e) {
            throw new IllegalStateException("Error while processing the class '" + className + "'", e);
        }
        byte[] bytes = aw.toByteArray();
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
            DexFile dexFile = dexFileLoader.loadDex(sourcePathName, sourcePathName + ".dex", 0);
            return classFileFactory.newClassFile(className, dexFile);
        } finally {
            FileUtils.delete(zip);
        }
    }

}

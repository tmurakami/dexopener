package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSource implements ClassSource {

    private final byte[] byteCode;
    private final Set<Set<String>> internalNamesSet;
    private final Map<String, DexFile> dexFileMap = new HashMap<>();
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final DexClassFileFactory classFileFactory;

    DexClassSource(byte[] byteCode,
                   Set<Set<String>> internalNamesSet,
                   File cacheDir,
                   DexFileLoader dexFileLoader,
                   DexClassFileFactory classFileFactory) {
        this.byteCode = byteCode;
        this.internalNamesSet = internalNamesSet;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.classFileFactory = classFileFactory;
    }

    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        DexFile dexFile = getDexFile(className);
        return dexFile == null ? null : classFileFactory.newClassFile(className, dexFile);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private DexFile getDexFile(String className) throws IOException {
        String internalName = DexUtils.toInternalName(className);
        DexFile dexFile = dexFileMap.get(internalName);
        if (dexFile != null) {
            return dexFile;
        }
        String[] classesToVisit = getClassesToVisit(internalName, internalNamesSet);
        if (classesToVisit == null) {
            return null;
        }
        ApplicationReader ar = new ApplicationReader(ASM4, byteCode);
        ApplicationWriter aw = new ApplicationWriter();
        ApplicationOpener opener = new ApplicationOpener(aw);
        try {
            ar.accept(opener, classesToVisit, 0);
        } catch (Exception e) {
            throw new IllegalStateException("Error while processing the class '" + className + "'", e);
        }
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
        File zip = File.createTempFile("classes", ".zip", cacheDir);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(aw.toByteArray());
            } finally {
                out.close();
            }
            String sourcePathName = zip.getCanonicalPath();
            dexFile = dexFileLoader.loadDex(sourcePathName, sourcePathName + ".dex", 0);
            for (String n : classesToVisit) {
                dexFileMap.put(n, dexFile);
            }
            return dexFile;
        } finally {
            FileUtils.delete(zip);
        }
    }

    private static String[] getClassesToVisit(String internalName,
                                              Set<Set<String>> internalNamesSet) {
        for (Set<String> s : internalNamesSet) {
            if (s.contains(internalName)) {
                return s.toArray(new String[s.size()]);
            }
        }
        return null;
    }

}

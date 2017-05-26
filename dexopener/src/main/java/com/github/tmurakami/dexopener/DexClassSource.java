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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSource implements ClassSource {

    private byte[] byteCode;
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
        this.internalNamesSet = new HashSet<>(internalNamesSet);
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.classFileFactory = classFileFactory;
    }

    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        DexFile dexFile = getDexFile(className);
        return dexFile == null ? null : classFileFactory.newClassFile(className, dexFile);
    }

    private DexFile getDexFile(String className) throws IOException {
        String internalName = DexUtils.toInternalName(className);
        DexFile dexFile = dexFileMap.get(internalName);
        if (dexFile != null) {
            return dexFile;
        }
        byte[] byteCode = this.byteCode;
        if (byteCode == null) {
            return null;
        }
        String[] classesToVisit = getClassesToVisit(internalName, internalNamesSet);
        if (classesToVisit == null) {
            return null;
        } else if (internalNamesSet.isEmpty()) {
            this.byteCode = null;
        }
        byte[] openedByteCode;
        try {
            openedByteCode = openClasses(byteCode, classesToVisit);
        } catch (Exception e) {
            throw new IllegalStateException("Error while processing the class '" + className + "'", e);
        }
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + cacheDir);
        }
        dexFile = loadDex(dexFileLoader, cacheDir, openedByteCode);
        for (String n : classesToVisit) {
            dexFileMap.put(n, dexFile);
        }
        return dexFile;
    }

    private static String[] getClassesToVisit(String internalName,
                                              Set<Set<String>> internalNamesSet) {
        for (Iterator<Set<String>> it = internalNamesSet.iterator(); it.hasNext(); ) {
            Set<String> set = it.next();
            if (set.contains(internalName)) {
                it.remove();
                return set.toArray(new String[set.size()]);
            }
        }
        return null;
    }

    private static byte[] openClasses(byte[] byteCode, String[] classesToVisit) {
        ApplicationWriter aw = new ApplicationWriter();
        new ApplicationReader(ASM4, byteCode).accept(new ApplicationOpener(aw), classesToVisit, 0);
        return aw.toByteArray();
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static DexFile loadDex(DexFileLoader dexFileLoader, File cacheDir, byte[] byteCode)
            throws IOException {
        File zip = File.createTempFile("classes", ".zip", cacheDir);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(byteCode);
            } finally {
                out.close();
            }
            String sourcePathName = zip.getCanonicalPath();
            return dexFileLoader.loadDex(sourcePathName, sourcePathName + ".dex", 0);
        } finally {
            FileUtils.delete(zip);
        }
    }

}

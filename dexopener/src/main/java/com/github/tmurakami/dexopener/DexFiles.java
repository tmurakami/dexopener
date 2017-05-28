package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexFiles {

    private byte[] byteCode;
    private final Set<Set<String>> internalNamesSet;
    private final File cacheDir;
    private final DexFileLoader dexFileLoader;
    private final Map<String, DexFile> dexFileMap;

    DexFiles(byte[] byteCode,
             Set<Set<String>> internalNamesSet,
             File cacheDir,
             DexFileLoader dexFileLoader,
             Map<String, DexFile> dexFileMap) {
        this.byteCode = byteCode;
        this.internalNamesSet = internalNamesSet;
        this.cacheDir = cacheDir;
        this.dexFileLoader = dexFileLoader;
        this.dexFileMap = dexFileMap;
    }

    DexFile get(String className) throws IOException {
        DexFile dexFile = dexFileMap.get(className);
        if (dexFile != null) {
            return dexFile;
        }
        byte[] byteCode = this.byteCode;
        if (byteCode == null) {
            return null;
        }
        String[] classesToVisit = getClassesToVisit(className, internalNamesSet);
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
        for (Enumeration<String> e = dexFile.entries(); e.hasMoreElements(); ) {
            dexFileMap.put(e.nextElement(), dexFile);
        }
        return dexFile;
    }

    private static String[] getClassesToVisit(String className, Set<Set<String>> internalNamesSet) {
        String internalName = TypeUtils.getInternalName(className);
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

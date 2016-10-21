package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

final class DexElementImpl implements DexElement {

    private static final String[] EMPTY_STRINGS = new String[0];
    private static final int CLASSES_PER_DEX_FILE = 100;

    private final ApplicationReader ar;
    private final File cacheDir;
    private final DexFileLoader fileLoader;
    private final Collection<String> unloadedClassNames;
    private final List<DexFile> dexFiles = new ArrayList<>();

    DexElementImpl(ApplicationReader ar,
                   File cacheDir,
                   ClassNameFilter classNameFilter,
                   DexFileLoader fileLoader) {
        this.ar = ar;
        this.cacheDir = cacheDir;
        this.fileLoader = fileLoader;
        this.unloadedClassNames = collectClassNames(ar, classNameFilter);
    }

    @Override
    public Class loadClass(String name, ClassLoader classLoader) {
        for (DexFile d : dexFiles) {
            Class<?> c = d.loadClass(name, classLoader);
            if (c != null) {
                return c;
            }
        }
        DexFile dexFile = loadDexFileFor(name);
        if (dexFile == null) {
            return null;
        }
        dexFiles.add(dexFile);
        return dexFile.loadClass(name, classLoader);
    }

    private DexFile loadDexFileFor(String name) {
        String[] classesToVisit = findClassesToVisit(name);
        if (classesToVisit.length == 0) {
            return null;
        }
        ApplicationWriter aw = new ApplicationWriter();
        ar.accept(new ApplicationOpener(aw), classesToVisit, 0);
        byte[] bytes = aw.toByteArray();
        File zip = null;
        try {
            zip = File.createTempFile("classes", ".zip", cacheDir);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.setMethod(ZipOutputStream.STORED);
                ZipEntry e = new ZipEntry("classes.dex");
                e.setSize(bytes.length);
                CRC32 crc32 = new CRC32();
                crc32.update(bytes);
                e.setCrc(crc32.getValue());
                out.putNextEntry(e);
                out.write(bytes);
            } finally {
                IOUtils.closeQuietly(out);
            }
            File dex = new File(cacheDir, zip.getName() + ".dex");
            return fileLoader.load(zip.getCanonicalPath(), dex.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.forceDelete(zip);
        }
    }

    private String[] findClassesToVisit(String name) {
        String className = 'L' + name.replace('.', '/') + ';';
        if (!unloadedClassNames.contains(className)) {
            return EMPTY_STRINGS;
        }
        Set<String> names = new HashSet<>();
        names.add(className);
        int slash = className.lastIndexOf('/');
        String pkg = slash == -1 ? null : className.substring(0, slash) + '/';
        for (Iterator<String> it = unloadedClassNames.iterator(); it.hasNext(); ) {
            String s = it.next();
            if (pkg == null || s.startsWith(pkg)) {
                names.add(s);
                it.remove();
                if (names.size() > CLASSES_PER_DEX_FILE) {
                    break;
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

    private static Collection<String> collectClassNames(ApplicationReader ar, ClassNameFilter classNameFilter) {
        DexFileReader r = (DexFileReader) ar.getDexFile();
        int size = r.getClassDefinitionsSize();
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            r.seek(r.getClassDefinitionOffset(i));
            String name = r.getStringItemFromTypeIndex(r.uint());
            if (classNameFilter.accept(name.substring(1, name.length() - 1).replace('/', '.'))) {
                names.add(name);
            }
        }
        Collections.sort(names);
        return names;
    }

}

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class InstallerImpl extends Installer {

    private final DexElementFactory elementFactory;
    private final ClassLoaderFactory classLoaderFactory;
    private final ClassLoaderHelper classLoaderHelper;

    InstallerImpl(DexElementFactory elementFactory,
                  ClassLoaderFactory classLoaderFactory,
                  ClassLoaderHelper classLoaderHelper) {
        this.elementFactory = elementFactory;
        this.classLoaderFactory = classLoaderFactory;
        this.classLoaderHelper = classLoaderHelper;
    }

    @Override
    public void install(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        File cacheDir = getCacheDir(ai.dataDir);
        Iterable<DexElement> elements = toDexElements(elementFactory, ai.sourceDir, cacheDir);
        ClassLoader classLoader = context.getClassLoader();
        classLoaderHelper.setParent(classLoader, classLoaderFactory.newClassLoader(classLoader, elements));
    }

    private static File getCacheDir(String dataDir) {
        File cacheDir = new File(dataDir, "code_cache/dexopener");
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new Error("Cannot create " + cacheDir);
        }
        IOUtils.deleteFiles(cacheDir.listFiles());
        return cacheDir;
    }

    private static Iterable<DexElement> toDexElements(DexElementFactory elementFactory,
                                                      String sourceDir,
                                                      File cacheDir) {
        List<DexElement> elements = new ArrayList<>();
        ZipInputStream in = null;
        try {
            in = new ZipInputStream(new FileInputStream(sourceDir));
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    elements.add(elementFactory.newDexElement(IOUtils.readBytes(in), cacheDir));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        if (elements.isEmpty()) {
            throw new Error(sourceDir + " does not contain the classes.dex");
        }
        return elements;
    }

}

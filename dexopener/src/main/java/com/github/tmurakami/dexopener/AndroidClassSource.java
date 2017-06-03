package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.classinjector.ClassSources;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class AndroidClassSource implements ClassSource {

    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private final DexFilesFactory dexFilesFactory;
    private final DexClassSourceFactory dexClassSourceFactory;
    private ClassSource delegate;

    AndroidClassSource(String sourceDir,
                       ClassNameFilter classNameFilter,
                       DexFilesFactory dexFilesFactory,
                       DexClassSourceFactory dexClassSourceFactory) {
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
        this.dexFilesFactory = dexFilesFactory;
        this.dexClassSourceFactory = dexClassSourceFactory;
    }

    @Override
    public ClassFile getClassFile(@NonNull String className) throws IOException {
        return classNameFilter.accept(className) ? getDelegate().getClassFile(className) : null;
    }

    private ClassSource getDelegate() throws IOException {
        ClassSource source = delegate;
        if (source == null) {
            source = delegate = newDelegate();
        }
        return source;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private ClassSource newDelegate() throws IOException {
        List<ClassSource> sources = new ArrayList<>();
        ClassNameReader r = new ClassNameReader(classNameFilter);
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (!name.startsWith("classes") || !name.endsWith(".dex")) {
                    continue;
                }
                Logger logger = Loggers.get();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Reading the entry " + name + " from " + sourceDir);
                }
                byte[] bytecode = IOUtils.readBytes(in);
                Set<String> classNames = r.read(new ApplicationReader(ASM4, bytecode));
                if (classNames.isEmpty()) {
                    continue;
                }
                if (logger.isLoggable(Level.FINEST)) {
                    for (String n : classNames) {
                        logger.finest("Class to be open: " + n);
                    }
                }
                DexFiles dexFiles = dexFilesFactory.newDexFiles(bytecode, classNames);
                sources.add(dexClassSourceFactory.newClassSource(dexFiles));
            }
        } finally {
            in.close();
        }
        return new ClassSources(sources);
    }

}

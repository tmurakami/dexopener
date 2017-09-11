package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class AndroidClassSource implements ClassSource {

    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private final DexFileHolderMapper dexFileHolderMapper;
    private final DexClassSourceFactory dexClassSourceFactory;
    private ClassSource delegate;

    AndroidClassSource(String sourceDir,
                       ClassNameFilter classNameFilter,
                       DexFileHolderMapper dexFileHolderMapper,
                       DexClassSourceFactory dexClassSourceFactory) {
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
        this.dexFileHolderMapper = dexFileHolderMapper;
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
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        Logger logger = Loggers.get();
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (!name.startsWith("classes") || !name.endsWith(".dex")) {
                    continue;
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Reading the entry " + name + " from " + sourceDir);
                }
                dexFileHolderMapper.map(IOUtils.readBytes(in), holderMap);
            }
        } finally {
            in.close();
        }
        if (holderMap.isEmpty()) {
            throw new IllegalStateException("There are no classes to be opened");
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(holderMap.size() + " classes will be opened");
        }
        return dexClassSourceFactory.newClassSource(Collections.unmodifiableMap(holderMap));
    }

}

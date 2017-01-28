package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.classinjector.ClassSources;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class ClassSourceImpl implements ClassSource {

    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private final DexClassSourceFactory dexClassSourceFactory;
    private ClassSource delegate;

    ClassSourceImpl(String sourceDir,
                    ClassNameFilter classNameFilter,
                    DexClassSourceFactory dexClassSourceFactory) {
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
        this.dexClassSourceFactory = dexClassSourceFactory;
    }

    @Override
    public ClassFile getClassFile(String className) throws IOException {
        return classNameFilter.accept(className) ? getDelegate().getClassFile(className) : null;
    }

    private ClassSource getDelegate() throws IOException {
        ClassSource source = delegate;
        if (source == null) {
            List<ClassSource> sources = new ArrayList<>();
            ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
            try {
                for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                    String name = e.getName();
                    if (name.startsWith("classes") && name.endsWith(".dex")) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[16384];
                        for (int l; (l = in.read(buffer)) != -1; ) {
                            out.write(buffer, 0, l);
                        }
                        sources.add(dexClassSourceFactory.newClassSource(out.toByteArray()));
                    }
                }
            } finally {
                in.close();
            }
            source = delegate = new ClassSources(sources);
        }
        return source;
    }

}

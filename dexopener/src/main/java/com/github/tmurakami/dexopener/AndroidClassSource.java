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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class AndroidClassSource implements ClassSource {

    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private final DexClassSourceFactory dexClassSourceFactory;
    private ClassSource delegate;

    AndroidClassSource(String sourceDir,
                       ClassNameFilter classNameFilter,
                       DexClassSourceFactory dexClassSourceFactory) {
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
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
        InternalNamesSetReader r = new InternalNamesSetReader(classNameFilter);
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    byte[] byteCode = IOUtils.readBytes(in);
                    ApplicationReader ar = new ApplicationReader(ASM4, byteCode);
                    Set<Set<String>> set = r.read(ar);
                    if (!set.isEmpty()) {
                        sources.add(dexClassSourceFactory.newClassSource(byteCode, set));
                    }
                }
            }
        } finally {
            in.close();
        }
        return new ClassSources(sources);
    }

}

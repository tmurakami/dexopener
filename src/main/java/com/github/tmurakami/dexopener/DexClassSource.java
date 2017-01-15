package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.classinjector.ClassSources;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class DexClassSource implements ClassSource {

    private final String sourceDir;
    private final ClassNameFilter classNameFilter;
    private ClassSource delegate;

    DexClassSource(String sourceDir, ClassNameFilter classNameFilter) {
        this.sourceDir = sourceDir;
        this.classNameFilter = classNameFilter;
    }

    @Override
    public byte[] getBytecodeFor(String name) throws IOException {
        return classNameFilter.accept(name) ? getDelegate().getBytecodeFor(name) : null;
    }

    private ClassSource getDelegate() throws IOException {
        if (delegate != null) {
            return delegate;
        }
        List<ClassSource> sources = new ArrayList<ClassSource>();
        ZipInputStream in = new ZipInputStream(new FileInputStream(sourceDir));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (!name.startsWith("classes") || !name.endsWith(".dex")) {
                    continue;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[16384];
                for (int l; (l = in.read(buffer)) != -1; ) {
                    out.write(buffer, 0, l);
                }
                final ApplicationReader ar = new ApplicationReader(ASM4, out.toByteArray());
                sources.add(new ClassSource() {
                    @Override
                    public byte[] getBytecodeFor(String name) throws IOException {
                        ApplicationWriter aw = new ApplicationWriter();
                        String[] classesToVisit = {'L' + name.replace('.', '/') + ';'};
                        ar.accept(new ApplicationOpener(aw), classesToVisit, 0);
                        return aw.toByteArray();
                    }
                });
            }
        } finally {
            in.close();
        }
        return delegate = new ClassSources(sources);
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class ClassesJarGeneratorImpl implements ClassesJarGenerator {

    private final ClassNameFilter classNameFilter;

    ClassesJarGeneratorImpl(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    @Override
    public String generateClassesJar(String apkPath, File cacheDir) throws IOException {
        File jar = File.createTempFile("classes-", ".jar", cacheDir);
        JarInputStream in = null;
        JarOutputStream out = null;
        try {
            in = new JarInputStream(new FileInputStream(apkPath));
            out = new JarOutputStream(new FileOutputStream(jar));
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.endsWith(".dex")) {
                    out.putNextEntry(new JarEntry(name));
                    ApplicationReader ar = new ApplicationReader(ASM4, readBytes(in));
                    ApplicationWriter aw = new ApplicationWriter(ar);
                    ar.accept(new InternalApplicationVisitor(ASM4, aw, classNameFilter), 0);
                    out.write(aw.toByteArray());
                    out.closeEntry();
                }
                in.closeEntry();
            }
        } finally {
            closeQuietly(in, out);
        }
        return jar.getCanonicalPath();
    }

    private static byte[] readBytes(JarInputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        for (int l; (l = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, l);
        }
        return out.toByteArray();
    }

    private static void closeQuietly(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }

}

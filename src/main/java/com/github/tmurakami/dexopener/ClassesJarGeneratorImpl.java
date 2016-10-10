package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.MethodVisitor;

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

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class ClassesJarGeneratorImpl implements ClassesJarGenerator {

    private final ClassNameFilter classNameFilter;

    ClassesJarGeneratorImpl(ClassNameFilter classNameFilter) {
        this.classNameFilter = classNameFilter;
    }

    @Override
    public String generateClassesJar(String apkPath, File cacheDir) throws IOException {
        File jar = File.createTempFile("classes", ".jar", cacheDir);
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

    private static class InternalApplicationVisitor extends ApplicationVisitor {

        private final ClassNameFilter classNameFilter;

        InternalApplicationVisitor(int api, ApplicationVisitor av, ClassNameFilter classNameFilter) {
            super(api, av);
            this.classNameFilter = classNameFilter;
        }

        @Override
        public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
            String className = name.substring(1, name.length() - 1).replace('/', '.');
            if (!classNameFilter.accept(className)) {
                return null;
            }
            return new InternalClassVisitor(api, super.visitClass(access & ~ACC_FINAL, name, signature, superName, interfaces));
        }

    }

    private static class InternalClassVisitor extends ClassVisitor {

        InternalClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access & ~ACC_FINAL);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
            return super.visitMethod(access & ~ACC_FINAL, name, desc, signature, exceptions);
        }

    }

}

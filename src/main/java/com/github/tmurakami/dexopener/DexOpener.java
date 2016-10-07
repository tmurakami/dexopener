package com.github.tmurakami.dexopener;

import android.content.Context;

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

import dalvik.system.DexClassLoader;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

public final class DexOpener extends AbstractAndroidJUnitRunner {

    private static final String[] IGNORED_PACKAGES = {
            "java.",
            "javax.",
            "junit.",
            "org.junit.",
            "org.hamcrest.",
            "android.support.annotation.",
            "android.support.multidex.",
            "android.support.test.",
            "org.mockito.",
            "net.bytebuddy.",
            "org.objenesis.",
            "com.android.dx.",
            "com.github.tmurakami.dexmockito.",
            "com.github.tmurakami.dexopener.",
    };

    @SuppressWarnings("WeakerAccess")
    static final String[] IGNORED_INTERNAL_PACKAGES;

    static {
        int length = IGNORED_PACKAGES.length;
        IGNORED_INTERNAL_PACKAGES = new String[length];
        for (int i = 0; i < length; i++) {
            IGNORED_INTERNAL_PACKAGES[i] = 'L' + IGNORED_PACKAGES[i].replace('.', '/');
        }
    }

    @Override
    ClassLoader newClassLoader(Context context, Context targetContext, ClassLoader parent) throws Exception {
        String apkPath = targetContext.getPackageCodePath();
        File cacheDir = targetContext.getDir("dexopener", Context.MODE_PRIVATE);
        File jar = File.createTempFile("classes-", ".jar", cacheDir);
        JarInputStream in = null;
        JarOutputStream out = null;
        try {
            in = new JarInputStream(new FileInputStream(apkPath));
            out = new JarOutputStream(new FileOutputStream(jar));
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.endsWith(".dex")) {
                    ByteArrayOutputStream o = new ByteArrayOutputStream(8192);
                    byte[] buffer = new byte[8192];
                    for (int l; (l = in.read(buffer)) != -1; ) {
                        o.write(buffer, 0, l);
                    }
                    out.putNextEntry(new JarEntry(name));
                    ApplicationReader ar = new ApplicationReader(ASM4, o.toByteArray());
                    ApplicationWriter aw = new ApplicationWriter(ar);
                    ar.accept(new InternalApplicationVisitor(ASM4, aw), 0);
                    out.write(aw.toByteArray());
                    out.closeEntry();
                }
                in.closeEntry();
            }
        } finally {
            closeQuietly(in, out);
        }
        String dexPath = jar.getCanonicalPath();
        String testApkPath = context.getPackageCodePath();
        if (!testApkPath.equals(apkPath)) {
            dexPath = testApkPath + ':' + dexPath;
        }
        return new InternalClassLoader(dexPath, cacheDir.getCanonicalPath(), parent);
    }

    @SuppressWarnings("WeakerAccess")
    static int open(int access) {
        return access & ~ACC_FINAL;
    }

    private static void closeQuietly(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static class InternalClassLoader extends DexClassLoader {

        InternalClassLoader(String dexPath, String optimizedDirectory, ClassLoader parent) {
            super(dexPath, optimizedDirectory, null, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (name.intern()) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    for (String pkg : IGNORED_PACKAGES) {
                        if (name.startsWith(pkg)) {
                            c = getParent().loadClass(name);
                            break;
                        }
                    }
                }
                if (c == null) {
                    try {
                        c = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                if (c == null) {
                    c = getParent().loadClass(name);
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }

    }

    private static class InternalApplicationVisitor extends ApplicationVisitor {

        InternalApplicationVisitor(int api, ApplicationVisitor av) {
            super(api, av);
        }

        @Override
        public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
            for (String pkg : IGNORED_INTERNAL_PACKAGES) {
                if (name.startsWith(pkg)) {
                    return null;
                }
            }
            return new InternalClassVisitor(api, super.visitClass(open(access), name, signature, superName, interfaces));
        }

    }

    private static class InternalClassVisitor extends ClassVisitor {

        InternalClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, open(access));
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
            return super.visitMethod(open(access), name, desc, signature, exceptions);
        }

    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

final class DexImpl implements Dex {

    private final Future<ApplicationReader> future;
    private final File cacheDir;
    private final DexFileLoader fileLoader;

    DexImpl(Future<ApplicationReader> future, File cacheDir, DexFileLoader fileLoader) {
        this.future = future;
        this.cacheDir = cacheDir;
        this.fileLoader = fileLoader;
    }

    @Override
    public Class loadClass(String name, ClassLoader classLoader) throws IOException {
        byte[] bytes = getBytes(name);
        if (bytes == null) {
            return null;
        }
        File zip = null;
        File dex = null;
        DexFile dexFile = null;
        try {
            zip = File.createTempFile("classes", ".zip", cacheDir);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
            try {
                out.putNextEntry(new ZipEntry("classes.dex"));
                out.write(bytes);
            } finally {
                closeQuietly(out);
            }
            dex = new File(cacheDir, zip.getName() + ".dex");
            dexFile = fileLoader.load(zip.getCanonicalPath(), dex.getCanonicalPath());
            return dexFile.loadClass(name, classLoader);
        } finally {
            closeQuietly(dexFile);
            deleteFiles(zip, dex);
        }
    }

    private byte[] getBytes(String name) throws IOException {
        String[] names = {'L' + name.replace('.', '/') + ';'};
        ApplicationReader ar = getApplicationReader();
        ApplicationWriter aw = new ApplicationWriter();
        ApplicationVisitor av = new InternalApplicationVisitor(aw);
        try {
            ar.accept(av, names, 0);
        } catch (NullPointerException e) {
            return null;
        }
        return aw.toByteArray();
    }

    private ApplicationReader getApplicationReader() throws IOException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void closeQuietly(DexFile dexFile) {
        if (dexFile != null) {
            try {
                dexFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void deleteFiles(File... files) {
        for (File f : files) {
            if (f != null && f.exists() && !f.delete()) {
                Logger.getLogger("com.github.tmurakami.dexopener").warning("Cannot delete " + f);
            }
        }
    }

}

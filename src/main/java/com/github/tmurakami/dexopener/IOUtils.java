package com.github.tmurakami.dexopener;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

final class IOUtils {

    private IOUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    static void closeQuietly(DexFile dexFile) {
        if (dexFile != null) {
            try {
                dexFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    static void closeQuietly(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    static void forceDelete(File file) {
        if (file == null) {
            return;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                forceDelete(f);
            }
        }
        if (file.exists() && !file.delete()) {
            Logger.getLogger("com.github.tmurakami.dexopener").warning("Cannot delete " + file);
        }
    }

}

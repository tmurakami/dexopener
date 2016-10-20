package com.github.tmurakami.dexopener;

import java.io.Closeable;
import java.io.IOException;
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

    static void closeQuietly(ZipFile dexFile) {
        if (dexFile != null) {
            try {
                dexFile.close();
            } catch (IOException ignored) {
            }
        }
    }

}

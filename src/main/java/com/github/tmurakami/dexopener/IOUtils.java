package com.github.tmurakami.dexopener;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

final class IOUtils {

    private IOUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static void closeQuietly(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable c : closeables) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    static void deleteFiles(File... files) {
        if (files != null) {
            for (File f : files) {
                if (f != null && f.exists()) {
                    if (f.isDirectory()) {
                        deleteFiles(f.listFiles());
                    }
                    if (!f.delete()) {
                        Logger.getLogger("com.github.tmurakami.dexopener").warning("Cannot delete " + f);
                    }
                }
            }
        }
    }

}

package com.github.tmurakami.dexopener;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[16384];
        for (int l; (l = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, l);
        }
        return out.toByteArray();
    }

}

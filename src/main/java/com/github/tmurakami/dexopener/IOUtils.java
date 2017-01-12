package com.github.tmurakami.dexopener;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

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

    static void closeQuietly(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {
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

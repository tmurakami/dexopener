package com.github.tmurakami.dexopener;

import java.io.Closeable;
import java.io.IOException;

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

}

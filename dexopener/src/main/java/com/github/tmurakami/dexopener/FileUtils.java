package com.github.tmurakami.dexopener;

import java.io.File;
import java.util.logging.Logger;

final class FileUtils {

    private FileUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static void delete(File... files) {
        for (File f : files) {
            if (f.isDirectory()) {
                delete(f.listFiles());
            }
            if (f.exists() && !f.delete()) {
                Logger.getLogger(BuildConfig.APPLICATION_ID).warning("Cannot delete " + f);
            }
        }
    }

}

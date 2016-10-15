package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class ApplicationReaderTask implements Callable<ApplicationReader> {

    private final File file;

    ApplicationReaderTask(File file) {
        this.file = file;
    }

    @Override
    public ApplicationReader call() throws Exception {
        ZipFile zipFile = new ZipFile(file);
        try {
            ZipEntry e = zipFile.getEntry("classes.dex");
            if (e == null) {
                throw new Error(file + " does not contain the classes.dex");
            }
            return new ApplicationReader(ASM4, zipFile.getInputStream(e));
        } finally {
            closeQuietly(zipFile);
        }
    }

    private void closeQuietly(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {
            }
        }
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;

final class ApplicationReaderTask implements Callable<ApplicationReader> {

    private final File file;

    ApplicationReaderTask(File file) {
        this.file = file;
    }

    @Override
    public ApplicationReader call() throws Exception {
        ZipInputStream in = new ZipInputStream(new FileInputStream(file));
        try {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                if (!e.getName().equals("classes.dex")) {
                    continue;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
                byte[] buffer = new byte[8192];
                for (int l; (l = in.read(buffer)) != -1; ) {
                    out.write(buffer, 0, l);
                }
                return new ApplicationReader(ASM4, out.toByteArray());
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        throw new Error(file + " does not contain the classes.dex");
    }

}

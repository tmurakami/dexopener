package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class DexGeneratorImpl implements DexGenerator {
    @Override
    public File generateDexFile(ApplicationReader ar,
                                File cacheDir,
                                String... classesToVisit) throws IOException {
        ApplicationWriter aw = new ApplicationWriter();
        ar.accept(new ApplicationOpener(aw), classesToVisit, 0);
        byte[] bytes = aw.toByteArray();
        File zip = File.createTempFile("classes", ".zip", cacheDir);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
        try {
            out.setMethod(ZipOutputStream.STORED);
            ZipEntry e = new ZipEntry("classes.dex");
            e.setSize(bytes.length);
            CRC32 crc32 = new CRC32();
            crc32.update(bytes);
            e.setCrc(crc32.getValue());
            out.putNextEntry(e);
            out.write(bytes);
        } finally {
            IOUtils.closeQuietly(out);
        }
        return zip;
    }
}

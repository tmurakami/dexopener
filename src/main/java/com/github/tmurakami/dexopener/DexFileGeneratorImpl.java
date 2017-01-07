package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

final class DexFileGeneratorImpl implements DexFileGenerator {

    private final DexFileLoader fileLoader;

    DexFileGeneratorImpl(DexFileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    @Override
    public DexFile generateDexFile(ApplicationReader ar,
                                   File cacheDir,
                                   Collection<String> classesToVisit) {
        ApplicationWriter aw = new ApplicationWriter();
        String[] names = classesToVisit.toArray(new String[classesToVisit.size()]);
        ar.accept(new ApplicationOpener(aw), names, 0);
        byte[] bytes = aw.toByteArray();
        File zip = null;
        try {
            zip = File.createTempFile("classes", ".zip", cacheDir);
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
            String sourcePathName = zip.getCanonicalPath();
            return fileLoader.load(sourcePathName, sourcePathName + ".dex");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.deleteFiles(zip);
        }
    }

}

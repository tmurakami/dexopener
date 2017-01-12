package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

final class DexFileGenerator {

    private final Transformer transformer;
    private final File cacheDir;
    private final DexFileHelper dexFileHelper;

    DexFileGenerator(Transformer transformer,
                     File cacheDir,
                     DexFileHelper dexFileHelper) {
        this.transformer = transformer;
        this.cacheDir = cacheDir;
        this.dexFileHelper = dexFileHelper;
    }

    DexFile generate(Collection<String> classNames) throws IOException {
        byte[] bytes = transformer.transform(classNames.toArray(new String[classNames.size()]));
        File zip = File.createTempFile("classes", ".zip", cacheDir);
        try {
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
            return dexFileHelper.loadDexFile(sourcePathName, sourcePathName + ".dex");
        } finally {
            IOUtils.deleteFiles(zip);
        }
    }

}

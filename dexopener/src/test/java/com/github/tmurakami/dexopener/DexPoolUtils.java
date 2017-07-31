package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.io.FileDataStore;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

final class DexPoolUtils {

    private DexPoolUtils() {
        throw new AssertionError("Do not instantiate");
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    static byte[] toBytecode(DexFile dexFile) throws IOException {
        File tmp = File.createTempFile("classes", ".dex");
        DexPool.writeTo(new FileDataStore(tmp), dexFile);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmp));
        try {
            return IOUtils.readBytes(in);
        } finally {
            in.close();
        }
    }

}

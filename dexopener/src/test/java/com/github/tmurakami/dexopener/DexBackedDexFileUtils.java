package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class DexBackedDexFileUtils {

    private DexBackedDexFileUtils() {
        throw new AssertionError("Do not instantiate");
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    static DexBackedDexFile loadDexFile(Opcodes opcodes, String path) throws IOException {
        InputStream in = new FileInputStream(path);
        try {
            return new DexBackedDexFile(opcodes, IOUtils.readBytes(in));
        } finally {
            in.close();
        }
    }

}

package com.github.tmurakami.dexopener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class IOUtils {

    private IOUtils() {
        throw new AssertionError("Do not instantiate");
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

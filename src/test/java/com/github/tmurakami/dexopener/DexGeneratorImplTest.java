package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertArrayEquals;

public class DexGeneratorImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testGenerateDexFile() throws IOException {
        String name = 'L' + getClass().getName().replace('.', '/') + ';';
        byte[] bytes = generateDex(name);
        File file = new DexGeneratorImpl().generateDexFile(new ApplicationReader(ASM4, bytes), folder.newFolder(), name);
        ZipFile zipFile = new ZipFile(file);
        try {
            InputStream in = zipFile.getInputStream(zipFile.getEntry("classes.dex"));
            ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
            byte[] buffer = new byte[8192];
            for (int l; (l = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, l);
            }
            assertArrayEquals(bytes, out.toByteArray());
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
    }

    private static byte[] generateDex(String name) {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        aw.visitClass(0, name, null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        return aw.toByteArray();
    }

}

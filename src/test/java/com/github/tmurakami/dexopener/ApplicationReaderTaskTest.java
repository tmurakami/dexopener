package com.github.tmurakami.dexopener;


import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.internal.util.io.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;

public class ApplicationReaderTaskTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCall() throws Exception {
        assertNotNull(new ApplicationReaderTask(generateZip()).call());
    }

    private File generateZip() throws IOException {
        String name = 'L' + getClass().getName().replace('.', '/') + "$C;";
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        aw.visitClass(0, name, null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        File file = folder.newFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        try {
            out.putNextEntry(new ZipEntry("foo"));
            out.closeEntry();
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(aw.toByteArray());
            out.closeEntry();
        } finally {
            IOUtil.closeQuietly(out);
        }
        return file;
    }

}

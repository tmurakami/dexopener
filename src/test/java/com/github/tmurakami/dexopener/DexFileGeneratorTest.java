package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexFileGeneratorTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    Transformer transformer;
    @Mock
    DexFileHelper dexFileHelper;
    @Mock
    DexFile dexFile;

    @Test
    public void testGenerateDexFile() throws IOException {
        String name = 'L' + getClass().getName().replace('.', '/') + ';';
        final byte[] bytes = generateDexBytes(name);
        given(transformer.transform(new String[]{name})).willReturn(bytes);
        final File cacheDir = folder.newFolder();
        given(dexFileHelper.loadDexFile(
                argThat(new ArgumentMatcher<String>() {
                    @Override
                    public boolean matches(String argument) {
                        File f = new File(argument);
                        String name = f.getName();
                        return f.exists()
                                && name.startsWith("classes")
                                && name.endsWith(".zip")
                                && f.getParentFile().equals(cacheDir)
                                && Arrays.equals(bytes, readDexBytes(f));
                    }
                }),
                argThat(new ArgumentMatcher<String>() {
                    @Override
                    public boolean matches(String argument) {
                        File f = new File(argument);
                        String name = f.getName();
                        return !f.exists()
                                && name.startsWith("classes")
                                && name.endsWith(".zip.dex")
                                && f.getParentFile().equals(cacheDir);
                    }
                }))).willReturn(dexFile);
        DexFileGenerator target = new DexFileGenerator(transformer, cacheDir, dexFileHelper);
        assertSame(dexFile, target.generate(Collections.singleton(name)));
    }

    private static byte[] generateDexBytes(String name) {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        aw.visitClass(0, name, null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        return aw.toByteArray();
    }

    @SuppressWarnings("WeakerAccess")
    static byte[] readDexBytes(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            return IOUtils.readBytes(zipFile.getInputStream(zipFile.getEntry("classes.dex")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexFileGeneratorImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    DexFileLoader fileLoader;
    @Mock
    DexFile dexFile;

    @InjectMocks
    DexFileGeneratorImpl target;

    @Test
    public void testGenerateDexFile() throws IOException {
        String name = 'L' + getClass().getName().replace('.', '/') + ';';
        final byte[] bytes = generateDexBytes(name);
        final File cacheDir = folder.newFolder();
        given(fileLoader.load(
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
        assertSame(dexFile, target.generateDexFile(new ApplicationReader(ASM4, bytes), cacheDir, name));
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
            return new ApplicationReader(ASM4, zipFile.getInputStream(zipFile.getEntry("classes.dex"))).byteCode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
    }

}

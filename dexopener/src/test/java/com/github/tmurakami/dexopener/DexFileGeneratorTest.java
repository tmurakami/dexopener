package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFileGeneratorTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private File cacheDir;

    @Mock
    private DexFileLoader dexFileLoader;

    @Mock
    private DexFile dexFile;

    @Captor
    private ArgumentCaptor<String> outputPathNameCaptor;

    @Test
    public void should_generate_the_dex_file_for_the_given_bytecode() throws Exception {
        String className = "foo.Bar";
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0,
                      TypeUtils.getInternalName(className),
                      null,
                      TypeUtils.getInternalName(Object.class.getName()),
                      null);
        aw.visitEnd();
        final byte[] bytecode = aw.toByteArray();
        final AtomicReference<String> sourcePathNameRef = new AtomicReference<>();
        given(dexFileLoader.loadDex(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                sourcePathNameRef.set(argument);
                return Arrays.equals(bytecode, readByteCode(argument));
            }
        }), outputPathNameCaptor.capture(), eq(0))).willReturn(dexFile);
        File cacheDir = folder.newFolder();
        assertSame(dexFile, new DexFileGenerator(cacheDir, dexFileLoader).generateDex(bytecode));
        File zip = new File(sourcePathNameRef.get());
        assertFalse(zip.exists());
        assertEquals(cacheDir, zip.getParentFile());
        assertTrue(zip.getName().startsWith("classes"));
        assertTrue(zip.getName().endsWith(".zip"));
        assertEquals(zip.getCanonicalPath() + ".dex", outputPathNameCaptor.getValue());
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_cache_dir_cannot_be_created()
            throws Exception {
        new DexFileGenerator(cacheDir, null).generateDex(null);
    }

    private static byte[] readByteCode(String zipPath) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipPath);
            ZipEntry e = zipFile.getEntry("classes.dex");
            return IOUtils.readBytes(zipFile.getInputStream(e));
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}

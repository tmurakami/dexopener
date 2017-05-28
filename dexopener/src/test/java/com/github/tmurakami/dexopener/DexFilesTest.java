package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFilesTest {

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
    public void should_generate_the_dex_file_for_the_given_name() throws Exception {
        String className = "foo.Bar";
        String internalName = TypeUtils.getInternalName(className);
        final byte[] byteCode = generateByteCode(internalName);
        final AtomicReference<String> sourcePathNameRef = new AtomicReference<>();
        given(dexFileLoader.loadDex(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                sourcePathNameRef.set(argument);
                return Arrays.equals(openClasses(byteCode), readByteCode(argument));
            }
        }), outputPathNameCaptor.capture(), eq(0))).willReturn(dexFile);
        given(dexFile.entries()).willReturn(Collections.enumeration(Collections.singleton(className)));
        Set<Set<String>> internalNamesSet = new HashSet<>();
        internalNamesSet.add(Collections.singleton(internalName));
        Map<String, DexFile> dexFileMap = new HashMap<>();
        File cacheDir = folder.newFolder();
        assertSame(dexFile, new DexFiles(byteCode,
                                         internalNamesSet,
                                         cacheDir,
                                         dexFileLoader,
                                         dexFileMap).get(className));
        assertTrue(internalNamesSet.isEmpty());
        assertSame(dexFile, dexFileMap.get(className));
        File zip = new File(sourcePathNameRef.get());
        assertFalse(zip.exists());
        assertEquals(cacheDir, zip.getParentFile());
        assertTrue(zip.getName().startsWith("classes"));
        assertTrue(zip.getName().endsWith(".zip"));
        assertEquals(zip.getCanonicalPath() + ".dex", outputPathNameCaptor.getValue());
    }

    @Test
    public void should_get_the_mapped_dex_file() throws Exception {
        String className = "foo.Bar";
        Map<String, DexFile> dexFileMap = new HashMap<>();
        dexFileMap.put(className, dexFile);
        assertSame(dexFile, new DexFiles(null,
                                         null,
                                         null,
                                         null,
                                         dexFileMap).get(className));
    }

    @Test
    public void should_get_null_if_the_byte_code_is_null() throws Exception {
        assertNull(new DexFiles(null,
                                null,
                                null,
                                null,
                                Collections.<String, DexFile>emptyMap()).get("foo.Bar"));
    }

    @Test
    public void should_get_null_if_the_given_name_is_not_in_the_list_of_internal_names()
            throws Exception {
        assertNull(new DexFiles(new byte[0],
                                Collections.<Set<String>>emptySet(),
                                null,
                                null,
                                Collections.<String, DexFile>emptyMap()).get("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_class_for_the_given_name_cannot_be_found()
            throws Exception {
        String className = "foo.Bar";
        String internalName = TypeUtils.getInternalName(className);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitEnd();
        Set<Set<String>> internalNamesSet = new HashSet<>();
        internalNamesSet.add(Collections.singleton(internalName));
        DexFiles dexFiles = new DexFiles(aw.toByteArray(),
                                         internalNamesSet,
                                         null,
                                         null,
                                         Collections.<String, DexFile>emptyMap());
        try {
            dexFiles.get(className);
        } finally {
            assertTrue(internalNamesSet.isEmpty());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_cache_dir_cannot_be_created()
            throws Exception {
        String className = "foo.Bar";
        String internalName = TypeUtils.getInternalName(className);
        byte[] byteCode = generateByteCode(internalName);
        Set<Set<String>> internalNamesSet = new HashSet<>();
        internalNamesSet.add(Collections.singleton(internalName));
        DexFiles dexFiles = new DexFiles(byteCode,
                                         internalNamesSet,
                                         cacheDir,
                                         null,
                                         Collections.<String, DexFile>emptyMap());
        try {
            dexFiles.get(className);
        } finally {
            assertTrue(internalNamesSet.isEmpty());
        }
    }

    private static byte[] generateByteCode(String internalName) {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(ACC_FINAL,
                      internalName,
                      null,
                      TypeUtils.getInternalName(Object.class.getName()),
                      null);
        aw.visitEnd();
        return aw.toByteArray();
    }

    private static byte[] openClasses(byte[] byteCode) {
        ApplicationWriter aw = new ApplicationWriter();
        new ApplicationReader(ASM4, byteCode).accept(new ApplicationOpener(aw), null, 0);
        return aw.toByteArray();
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

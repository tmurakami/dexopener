package com.github.tmurakami.classinjector.android;

import com.github.tmurakami.classinjector.ClassFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexBytesClassFileTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    File cacheDir;
    @Mock
    DexFileLoader dexFileLoader;
    @Mock
    DexFile dexFile;
    @Mock
    ClassLoader classLoader;
    @Captor
    ArgumentCaptor<String> sourceCaptor;
    @Captor
    ArgumentCaptor<String> outputCaptor;

    @Test(expected = IllegalArgumentException.class)
    public void _new_nullClassName() throws Exception {
        new DexBytesClassFile(null, new byte[0], cacheDir);
    }

    @Test(expected = IllegalArgumentException.class)
    public void _new_nullBytes() throws Exception {
        new DexBytesClassFile("foo.Bar", null, cacheDir);
    }

    @Test(expected = IllegalArgumentException.class)
    public void _new_nullCacheDir() throws Exception {
        new DexBytesClassFile("foo.Bar", new byte[0], null);
    }

    @Test
    public void toClass() throws Exception {
        File cacheDir = folder.newFolder();
        given(dexFileLoader.loadDex(sourceCaptor.capture(), outputCaptor.capture(), eq(0))).willReturn(dexFile);
        Class<?> c = getClass();
        given(dexFile.loadClass("foo.Bar", classLoader)).willReturn(c);
        byte[] bytes = "abc".getBytes();
        assertSame(c, new DexBytesClassFile("foo.Bar", bytes, cacheDir, dexFileLoader).toClass(classLoader));
        String source = sourceCaptor.getValue();
        File sourceFile = new File(source);
        assertEquals(cacheDir, sourceFile.getParentFile());
        assertTrue(sourceFile.getName().startsWith("classes"));
        assertTrue(source.endsWith(".zip"));
        ZipFile zipFile = new ZipFile(sourceFile);
        try {
            ZipEntry e = zipFile.getEntry("classes.dex");
            assertNotNull(e);
            InputStream in = zipFile.getInputStream(e);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            for (int l; (l = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, l);
            }
            assertArrayEquals(bytes, out.toByteArray());
        } finally {
            zipFile.close();
        }
        assertEquals(source + ".dex", outputCaptor.getValue());
    }

    @Test(expected = IllegalStateException.class)
    public void toClass_cacheDirCannotCreate() throws Exception {
        new DexBytesClassFile("foo.Bar", new byte[0], cacheDir).toClass(classLoader);
    }

    @Test
    public void close() throws Exception {
        File cacheDir = folder.newFolder();
        given(dexFileLoader.loadDex(anyString(), anyString(), eq(0))).willReturn(dexFile);
        ClassFile file = new DexBytesClassFile("foo.Bar", new byte[0], cacheDir, dexFileLoader);
        file.toClass(classLoader);
        file.close();
        String[] list = cacheDir.list();
        assertNotNull(list);
        assertEquals(0, list.length);
    }

}

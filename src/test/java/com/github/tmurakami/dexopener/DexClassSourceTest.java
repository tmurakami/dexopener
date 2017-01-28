package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class DexClassSourceTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    ApplicationReader applicationReader;
    @Mock
    Set<String> classNames;
    @Mock
    File cacheDir;
    @Mock
    DexFileLoader dexFileLoader;
    @Mock
    DexClassFileFactory classFileFactory;
    @Mock
    DexFile dexFile;
    @Mock
    ClassFile classFile;

    @Test
    public void getClassFile() throws Exception {
        final File cacheDir = folder.newFolder();
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0, "Lfoo/Bar;", null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        final byte[] bytes = aw.toByteArray();
        given(classNames.contains("foo.Bar")).willReturn(true);
        given(dexFileLoader.loadDex(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                File f = new File(argument);
                String name = f.getName();
                if (!cacheDir.equals(f.getParentFile()) || !name.startsWith("classes") || !name.endsWith(".zip")) {
                    return false;
                }
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(f);
                    ZipEntry e = zipFile.getEntry("classes.dex");
                    assertNotNull(e);
                    InputStream in = zipFile.getInputStream(e);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[16384];
                    for (int l; (l = in.read(buffer)) != -1; ) {
                        out.write(buffer, 0, l);
                    }
                    return Arrays.equals(bytes, out.toByteArray());
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
        }), argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                File f = new File(argument);
                String name = f.getName();
                return cacheDir.equals(f.getParentFile()) && name.startsWith("classes") && name.endsWith(".zip.dex");
            }
        }), eq(0))).willReturn(dexFile);
        given(classFileFactory.create("foo.Bar", dexFile)).willReturn(classFile);
        ApplicationReader ar = new ApplicationReader(ASM4, bytes);
        assertSame(classFile, new DexClassSource(ar, classNames, cacheDir, dexFileLoader, classFileFactory).getClassFile("foo.Bar"));
    }

    @Test
    public void getClassFile_classNotFound() throws Exception {
        assertNull(new DexClassSource(applicationReader, classNames, cacheDir, dexFileLoader, classFileFactory).getClassFile("foo.Bar"));
        then(applicationReader).should(never()).accept(any(ApplicationOpener.class), any(String[].class), eq(0));
    }

    @Test
    public void getClassFile_nullBytes() throws Exception {
        given(classNames.contains("foo.Bar")).willReturn(true);
        assertNull(new DexClassSource(applicationReader, classNames, cacheDir, dexFileLoader, classFileFactory).getClassFile("foo.Bar"));
        then(applicationReader).should().accept(any(ApplicationOpener.class), any(String[].class), eq(0));
    }

    @Test(expected = IllegalStateException.class)
    public void getClassFile_cannotCreateCacheDir() throws Exception {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0, "Lfoo/Bar;", null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        byte[] bytes = aw.toByteArray();
        given(classNames.contains("foo.Bar")).willReturn(true);
        new DexClassSource(new ApplicationReader(ASM4, bytes), classNames, cacheDir, dexFileLoader, classFileFactory).getClassFile("foo.Bar");
    }

}

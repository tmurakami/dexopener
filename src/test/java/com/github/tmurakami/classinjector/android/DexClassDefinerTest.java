package com.github.tmurakami.classinjector.android;

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
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DexClassDefinerTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    DexClassDefiner.DexFileLoader dexFileLoader;
    @Mock
    ClassLoader classLoader;
    @Mock
    DexFile dexFile;

    @Test
    public void defineClass() throws IOException {
        final File cacheDir = folder.newFolder();
        final byte[] bytecode = {};
        given(dexFileLoader.loadDex(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                File f = new File(argument);
                File p = f.getParentFile();
                String n = f.getName();
                if (!p.equals(cacheDir) || !n.startsWith("classes") || !n.endsWith(".zip")) {
                    return false;
                }
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(f);
                    InputStream in = zipFile.getInputStream(zipFile.getEntry("classes.dex"));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[16384];
                    for (int l; (l = in.read(buffer)) != -1; ) {
                        out.write(buffer, 0, l);
                    }
                    return Arrays.equals(out.toByteArray(), bytecode);
                } catch (IOException e) {
                    return false;
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
                File p = f.getParentFile();
                String n = f.getName();
                return p.equals(cacheDir) && n.startsWith("classes") && n.endsWith(".zip.dex");
            }
        }), eq(0))).willReturn(dexFile);
        new DexClassDefiner(dexFileLoader, cacheDir).defineClass("foo.Bar", bytecode, classLoader);
    }

    @Test(expected = IllegalStateException.class)
    public void defineClass_cannotCreateCacheDir() {
        new DexClassDefiner(mock(File.class)).defineClass("foo.Bar", new byte[0], classLoader);
    }

    @Test(expected = IOError.class)
    public void defineClass_ioExceptionWhiteLoadingDex() throws IOException {
        File cacheDir = folder.newFolder();
        given(dexFileLoader.loadDex(anyString(), anyString(), eq(0))).willThrow(IOException.class);
        new DexClassDefiner(dexFileLoader, cacheDir).defineClass("foo.Bar", new byte[0], classLoader);
    }

}

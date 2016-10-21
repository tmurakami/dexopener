package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import dalvik.system.DexFile;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexElementImplTest {

    @Mock
    ApplicationReader ar;
    @Mock
    DexGenerator fileGenerator;
    @Mock
    DexFileLoader fileLoader;
    @Mock
    Callable<DexFile> task;
    @Mock
    DexFile dexFile;
    @Mock
    ClassLoader classLoader;

    @Test
    public void testLoadClass() throws IOException {
        String name = 'L' + C.class.getName().replace('.', '/') + ';';
        List<String> classNames = Collections.singletonList(name);
        File cacheDir = new File("cacheDir");
        File file = new File("classes.zip");
        given(fileGenerator.generateDexFile(ar, cacheDir, name)).willReturn(file);
        given(fileLoader.load(file.getCanonicalPath(), new File(cacheDir, "classes.zip.dex").getCanonicalPath())).willReturn(dexFile);
        given(dexFile.loadClass(C.class.getName(), classLoader)).willReturn(C.class);
        assertSame(C.class, new DexElementImpl(ar, classNames, cacheDir, fileGenerator, fileLoader).loadClass(C.class.getName(), classLoader));
    }

    private static class C {
    }

}

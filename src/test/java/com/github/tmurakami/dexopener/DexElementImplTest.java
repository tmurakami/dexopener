package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Collections;
import java.util.List;

import dalvik.system.DexFile;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexElementImplTest {

    @Mock
    ApplicationReader ar;
    @Mock
    DexFileGenerator fileGenerator;
    @Mock
    DexFile dexFile;
    @Mock
    ClassLoader classLoader;

    @Test
    public void testLoadClass() {
        String name = 'L' + C.class.getName().replace('.', '/') + ';';
        List<String> classNames = Collections.singletonList(name);
        File cacheDir = new File("cacheDir");
        given(fileGenerator.generateDexFile(ar, cacheDir, name)).willReturn(dexFile);
        given(dexFile.loadClass(C.class.getName(), classLoader)).willReturn(C.class);
        assertSame(C.class, new DexElementImpl(ar, classNames, cacheDir, fileGenerator).loadClass(C.class.getName(), classLoader));
    }

    private static class C {
    }

}

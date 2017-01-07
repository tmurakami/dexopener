package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        File cacheDir = new File("cacheDir");
        Set<String> classNames = Collections.singleton('L' + C.class.getName().replace('.', '/') + ';');
        given(fileGenerator.generateDexFile(ar, cacheDir, classNames)).willReturn(dexFile);
        given(dexFile.loadClass(C.class.getName(), classLoader)).willReturn(C.class);
        Set<Set<String>> classNamesSet = Collections.singleton(classNames);
        ConcurrentHashMap<Set<String>, DexFile> dexFileMap = new ConcurrentHashMap<>();
        assertSame(C.class, new DexElementImpl(ar, cacheDir, fileGenerator, classNamesSet, dexFileMap).loadClass(C.class.getName(), classLoader));
    }

    private static class C {
    }

}

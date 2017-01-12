package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexFile;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexElementTest {

    @Mock
    DexFileGenerator fileGenerator;
    @Mock
    DexFile dexFile;
    @Mock
    ClassLoader classLoader;

    @Test
    public void testLoadClass() throws IOException {
        Set<String> classNames = Collections.singleton('L' + C.class.getName().replace('.', '/') + ';');
        given(fileGenerator.generate(classNames)).willReturn(dexFile);
        given(dexFile.loadClass(C.class.getName(), classLoader)).willReturn(C.class);
        Set<Set<String>> classNamesSet = Collections.singleton(classNames);
        ConcurrentHashMap<Set<String>, DexFile> dexFileMap = new ConcurrentHashMap<>();
        assertSame(C.class, new DexElement(fileGenerator, classNamesSet, dexFileMap).loadClass(C.class.getName(), classLoader));
    }

    private static class C {
    }

}

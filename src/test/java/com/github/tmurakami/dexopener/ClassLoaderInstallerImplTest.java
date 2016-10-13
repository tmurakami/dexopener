package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import dalvik.system.DexFile;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderInstallerImplTest {

    @Mock
    ClassLoader parent;
    @Mock
    DexFile dexFile;

    @Test
    public void testInstall() throws ClassNotFoundException {
        ClassLoader classLoader = new ClassLoader(parent) {
        };
        given(dexFile.loadClass("test", classLoader)).willReturn(C.class);
        new ClassLoaderInstallerImpl().install(classLoader, Collections.singletonList(dexFile));
        assertSame(C.class, classLoader.loadClass("test"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInstall_delegation() throws ClassNotFoundException {
        given(parent.loadClass("test")).willReturn((Class) C.class);
        ClassLoader classLoader = new ClassLoader(parent) {
        };
        new ClassLoaderInstallerImpl().install(classLoader, Collections.singletonList(dexFile));
        assertSame(C.class, classLoader.loadClass("test"));
    }

    private static class C {
    }

}

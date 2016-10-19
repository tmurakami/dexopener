package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class OpenedClassLoaderTest {

    @Mock
    ClassLoader classLoader;
    @Mock
    ClassNameFilter classNameFilter;
    @Mock
    DexElement element;

    @InjectMocks
    OpenedClassLoader target;

    @Test
    public void testFindClass_found() throws ClassNotFoundException {
        given(classNameFilter.accept("a")).willReturn(true);
        given(element.loadClass("a", classLoader)).willReturn(C.class);
        assertSame(C.class, target.findClass("a"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_notFound() throws ClassNotFoundException {
        given(classNameFilter.accept("a")).willReturn(false);
        try {
            target.findClass("a");
        } finally {
            then(element).should(never()).loadClass("a", classLoader);
        }
    }

    private static class C {
    }

}

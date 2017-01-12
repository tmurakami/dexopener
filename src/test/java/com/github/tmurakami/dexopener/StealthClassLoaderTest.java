package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class StealthClassLoaderTest {

    @Mock
    ClassLoader classLoader;
    @Mock
    ClassNameFilter classNameFilter;
    @Mock
    Iterable<DexElement> elements;
    @Mock
    Iterator<DexElement> iterator;
    @Mock
    DexElement element;

    @InjectMocks
    StealthClassLoader target;

    @Test
    public void testFindClass_found() throws ClassNotFoundException, IOException {
        given(classNameFilter.accept("a")).willReturn(true);
        given(elements.iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(element).willThrow(NoSuchElementException.class);
        given(element.loadClass("a", classLoader)).willReturn(C.class);
        assertSame(C.class, target.findClass("a"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_notFound() throws ClassNotFoundException {
        given(classNameFilter.accept("a")).willReturn(false);
        try {
            target.findClass("a");
        } finally {
            then(elements).should(never()).iterator();
        }
    }

    private static class C {
    }

}

package com.github.tmurakami.dexopener;

import net.bytebuddy.ByteBuddy;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.objenesis.Objenesis;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(Parameterized.class)
public class OpenedClassLoaderTest {

    private final String className;
    private final Class<?> expected;

    @Mock
    Iterable<DexElement> elements;
    @Mock
    Iterator<DexElement> iterator;
    @Mock
    DexElement element;
    @Mock
    ClassLoader classLoader;

    @InjectMocks
    OpenedClassLoader target;

    public OpenedClassLoaderTest(String className, Class<?> expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "className={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{"android.support.v4.app.Fragment", null},
                new Object[]{"com.android.dx.Version", null},
                new Object[]{"com.android.test.runner.MultiDexTestRunner", null},
                new Object[]{"com.github.tmurakami.dexmockito.DexMockitoMockMaker", null},
                new Object[]{DexOpener.class.getName(), null},
                new Object[]{"kotlin.Unit", null},
                new Object[]{ByteBuddy.class.getName(), null},
                new Object[]{CoreMatchers.class.getName(), null},
                new Object[]{Test.class.getName(), null},
                new Object[]{Mockito.class.getName(), null},
                new Object[]{Objenesis.class.getName(), null},
                new Object[]{"foo.BuildConfig", null},
                new Object[]{"foo.R", null},
                new Object[]{"foo.R$string", null},
                new Object[]{"BuildConfig", C.class},
                new Object[]{"R", C.class},
                new Object[]{"R$string", C.class},
                new Object[]{"foo.Bar$BuildConfig", C.class},
                new Object[]{"foo.Bar$R", C.class},
                new Object[]{"foo.Bar$R$string", C.class},
                new Object[]{"foo.Bar", C.class});
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAccept() throws ClassNotFoundException {
        given(elements.iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(element).willThrow(NoSuchElementException.class);
        given(element.loadClass(className, classLoader)).willReturn(C.class);
        assertSame(expected, target.loadClass(className));
    }

    private static class C {
    }

}

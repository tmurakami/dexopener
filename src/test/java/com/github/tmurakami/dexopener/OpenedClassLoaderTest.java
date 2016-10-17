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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(Parameterized.class)
public class OpenedClassLoaderTest {

    private final List<DexElement> elements = new ArrayList<>();
    private final String className;
    private final Class<?> expected;

    @Mock
    DexElement element;
    @Mock
    OpenedClassLoader.SuperCalls superCalls;

    @InjectMocks
    OpenedClassLoader target = new OpenedClassLoader(getClass().getClassLoader(), elements);

    public OpenedClassLoaderTest(String className, Class<?> expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "className={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{"android.databinding.DataBindingUtil", C.class},
                new Object[]{"android.support.v4.app.Fragment", C.class},
                new Object[]{"com.android.dx.Version", C.class},
                new Object[]{"com.android.test.runner.MultiDexTestRunner", C.class},
                new Object[]{"com.github.tmurakami.dexmockito.DexMockitoMockMaker", C.class},
                new Object[]{DexOpener.class.getName(), C.class},
                new Object[]{"kotlin.Unit", C.class},
                new Object[]{ByteBuddy.class.getName(), C.class},
                new Object[]{CoreMatchers.class.getName(), C.class},
                new Object[]{Test.class.getName(), C.class},
                new Object[]{Mockito.class.getName(), C.class},
                new Object[]{Objenesis.class.getName(), C.class},
                new Object[]{"foo.BuildConfig", C.class},
                new Object[]{"foo.R", C.class},
                new Object[]{"foo.R$string", C.class},
                new Object[]{"BuildConfig", D.class},
                new Object[]{"R", D.class},
                new Object[]{"R$string", D.class},
                new Object[]{"foo.Bar$BuildConfig", D.class},
                new Object[]{"foo.Bar$R", D.class},
                new Object[]{"foo.Bar$R$string", D.class},
                new Object[]{"foo.Bar", D.class});
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        elements.add(element);
    }

    @Test
    public void testAccept() throws ClassNotFoundException {
        given(superCalls.findClass(className)).willReturn(C.class);
        given(element.loadClass(className, getClass().getClassLoader())).willReturn(D.class);
        assertSame(expected, target.loadClass(className));
    }

    private static class C {
    }

    private static class D {
    }

}

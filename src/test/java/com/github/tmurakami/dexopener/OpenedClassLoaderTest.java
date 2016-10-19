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
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(Parameterized.class)
public class OpenedClassLoaderTest {

    private final Tester tester;

    @Mock
    ClassLoader classLoader;
    @Mock
    Iterable<DexElement> elements;
    @Mock
    Iterator<DexElement> iterator;
    @Mock
    DexElement element;

    @InjectMocks
    OpenedClassLoader target;

    public OpenedClassLoaderTest(Tester tester) {
        this.tester = tester;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Tester> parameters() {
        return Arrays.asList(
                notFound("android.databinding.DataBindingUtil"),
                notFound("android.support.v4.app.Fragment"),
                notFound("com.android.dx.Version"),
                notFound("com.android.test.runner.MultiDexTestRunner"),
                notFound("com.github.tmurakami.dexmockito.DexMockitoMockMaker"),
                notFound(DexOpener.class.getName()),
                notFound("kotlin.Unit"),
                notFound(ByteBuddy.class.getName()),
                notFound(CoreMatchers.class.getName()),
                notFound(Test.class.getName()),
                notFound(Mockito.class.getName()),
                notFound(Objenesis.class.getName()),
                notFound("foo.R"),
                notFound("foo.R$string"),
                found("R"),
                found("R$string"),
                found("foo.Bar$R"),
                found("foo.Bar$R$string"),
                found("foo.Bar"));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindClass() {
        given(elements.iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(element).willThrow(NoSuchElementException.class);
        tester.test(target, element, classLoader);
    }

    private static class C {
    }

    private interface TestExecutor {
        void test(String name, OpenedClassLoader target, DexElement element, ClassLoader classLoader);
    }

    private static class Tester {

        private final String name;
        private final TestExecutor executor;

        Tester(String name, TestExecutor executor) {
            this.name = name;
            this.executor = executor;
        }

        @Override
        public String toString() {
            return "name=" + name;
        }

        void test(OpenedClassLoader target, DexElement element, ClassLoader classLoader) {
            executor.test(name, target, element, classLoader);
        }

    }

    private static Tester found(String name) {
        return new Tester(name, new TestExecutor() {
            @Override
            public void test(String name, OpenedClassLoader target, DexElement element, ClassLoader classLoader) {
                given(element.loadClass(name, classLoader)).willReturn(C.class);
                Class<?> c = null;
                try {
                    c = target.findClass(name);
                } catch (ClassNotFoundException e) {
                    fail();
                }
                assertSame(C.class, c);
            }
        });
    }

    private static Tester notFound(String name) {
        return new Tester(name, new TestExecutor() {
            @Override
            public void test(String name, OpenedClassLoader target, DexElement element, ClassLoader classLoader) {
                try {
                    target.findClass(name);
                    fail();
                } catch (ClassNotFoundException e) {
                    then(element).should(never()).loadClass(name, classLoader);
                }
            }
        });
    }

}

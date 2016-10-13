package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ClassNameFilterImplTest {

    private final ClassNameFilterImpl target = new ClassNameFilterImpl();

    private final String name;
    private final boolean expected;

    public ClassNameFilterImplTest(String name, boolean expected) {
        this.name = name;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{"Landroid/support/annotation/AnimatorRes;", false},
                new Object[]{"Landroid/support/multidex/MultiDex;", false},
                new Object[]{"Landroid/support/test/InstrumentationRegistry;", false},
                new Object[]{"Lcom/android/dex/ClassData;", false},
                new Object[]{"Lcom/android/dx/Version;", false},
                new Object[]{"Lcom/android/internal/util/Predicate;", false},
                new Object[]{"Lcom/github/tmurakami/dexmockito/DexMockitoMockMaker;", false},
                new Object[]{"Lcom/github/tmurakami/dexopener/ClassNameFilterImpl;", false},
                new Object[]{"Ljunit/framework/TestCase;", false},
                new Object[]{"Lnet/bytebuddy/ByteBuddy;", false},
                new Object[]{"Lorg/hamcrest/CoreMatchers;", false},
                new Object[]{"Lorg/junit/Test;", false},
                new Object[]{"Lorg/mockito/Mockito;", false},
                new Object[]{"Lorg/objenesis/Objenesis;", false},
                new Object[]{"Lfoo/R;", false},
                new Object[]{"Lfoo/R$string;", false},
                new Object[]{"Lfoo/BuildConfig;", false},
                new Object[]{"LFoo;", true},
                new Object[]{"Lfoo/Bar;", true},
                new Object[]{"Lfoo/Bar$Baz;", true});
    }

    @Test
    public void testAccept() {
        assertEquals(expected, target.accept(name));
    }

}

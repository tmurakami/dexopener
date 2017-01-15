package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertSame;

@RunWith(Parameterized.class)
public class ClassNameFilterTest {

    private final ClassNameFilter target = new ClassNameFilter();

    private final String name;
    private final boolean expected;

    public ClassNameFilterTest(String name, boolean expected) {
        this.name = name;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{"android.C", false},
                new Object[]{"com.android.C", false},
                new Object[]{"com.github.tmurakami.dexmockito.C", false},
                new Object[]{DexOpener.class.getName(), false},
                new Object[]{"junit.C", false},
                new Object[]{"kotlin.C", false},
                new Object[]{"kotlinx.C", false},
                new Object[]{"net.bytebuddy.C", false},
                new Object[]{"org.hamcrest.C", false},
                new Object[]{"org.junit.C", false},
                new Object[]{"org.mockito.C", false},
                new Object[]{"org.objenesis.C", false},
                new Object[]{"foo.R", false},
                new Object[]{"foo.R$string", false},
                new Object[]{"R", true},
                new Object[]{"R$string", true},
                new Object[]{"foo.Bar$R", true},
                new Object[]{"foo.Bar$R$string", true},
                new Object[]{"foo.Bar", true});
    }

    @Test
    public void testAccept() {
        assertSame(expected, target.accept(name));
    }

}

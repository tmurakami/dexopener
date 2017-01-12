package com.github.tmurakami.dexopener;

import net.bytebuddy.ByteBuddy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.objenesis.Objenesis;

import java.util.Arrays;

import static org.junit.Assert.assertSame;

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
                new Object[]{"com.github.tmurakami.dexmockito.DexMockitoMockMaker", false},
                new Object[]{ByteBuddy.class.getName(), false},
                new Object[]{Mockito.class.getName(), false},
                new Object[]{Objenesis.class.getName(), false},
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

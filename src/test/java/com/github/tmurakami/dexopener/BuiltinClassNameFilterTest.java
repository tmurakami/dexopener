package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertSame;

@RunWith(Parameterized.class)
public class BuiltinClassNameFilterTest {

    private final BuiltinClassNameFilter testTarget = BuiltinClassNameFilter.INSTANCE;

    private final String className;
    private final boolean expected;

    public BuiltinClassNameFilterTest(String className, boolean expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{"android.C", false},
                new Object[]{"com.android.C", false},
                new Object[]{"com.github.tmurakami.classinjector.C", false},
                new Object[]{"com.github.tmurakami.dexmockito.C", false},
                new Object[]{DexOpener.class.getName(), false},
                new Object[]{"com.github.tmurakami.mockito4k.C", false},
                new Object[]{"java.C", false},
                new Object[]{"javax.C", false},
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
                new Object[]{"foo.BuildConfig", false},
                new Object[]{"R", true},
                new Object[]{"R$string", true},
                new Object[]{"foo.Bar$R", true},
                new Object[]{"foo.Bar$R$string", true},
                new Object[]{"foo.Bar", true});
    }

    @Test
    public void accept() throws Exception {
        assertSame(expected, testTarget.accept(className));
    }

}

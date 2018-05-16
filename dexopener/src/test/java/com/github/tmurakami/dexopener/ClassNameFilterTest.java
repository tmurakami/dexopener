package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertSame;

@SuppressWarnings("deprecation")
@RunWith(Parameterized.class)
public class ClassNameFilterTest {

    private static final boolean ALLOW = true;
    private static final boolean DENY = false;

    private final ClassNameFilter testTarget = new ClassNameFilter("foo.");

    private final String className;
    private final boolean expected;

    public ClassNameFilterTest(String className, boolean expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[]{"foo.R", DENY},
                             new Object[]{"foo.R$string", DENY},
                             new Object[]{"foo.BuildConfig", DENY},
                             new Object[]{"foo.BR", DENY},
                             new Object[]{"C", DENY},
                             new Object[]{"android.databinding.DataBinderMapper", ALLOW},
                             new Object[]{"android.databinding.DataBindingComponent", ALLOW},
                             new Object[]{"android.databinding.DataBindingUtil", ALLOW},
                             new Object[]{"android.databinding.generated.C", ALLOW},
                             new Object[]{"foo.Bar$R", ALLOW},
                             new Object[]{"foo.Bar$R$string", ALLOW},
                             new Object[]{"foo.Bar$BuildConfig", ALLOW},
                             new Object[]{"foo.Bar$BR", ALLOW},
                             new Object[]{"foo.Bar", ALLOW});
    }

    @Test
    public void accept_should_return_a_value_equal_to_the_expected_value() {
        assertSame(expected, testTarget.accept(className));
    }

}

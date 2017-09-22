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
        return Arrays.asList(new Object[]{"android.C", DENY},
                             new Object[]{"android.databinding.C", DENY},
                             new Object[]{"com.android.C", DENY},
                             new Object[]{"com.github.tmurakami.classinjector.C", DENY},
                             new Object[]{"com.github.tmurakami.dexmockito.C", DENY},
                             new Object[]{DexOpener.class.getName(), DENY},
                             new Object[]{"com.github.tmurakami.mockito4k.C", DENY},
                             new Object[]{"dalvik.C", DENY},
                             new Object[]{"java.C", DENY},
                             new Object[]{"javax.C", DENY},
                             new Object[]{"junit.C", DENY},
                             new Object[]{"junitparams.C", DENY},
                             new Object[]{"kotlin.C", DENY},
                             new Object[]{"kotlinx.C", DENY},
                             new Object[]{"net.bytebuddy.C", DENY},
                             new Object[]{"org.apache.http.C", DENY},
                             new Object[]{"org.hamcrest.C", DENY},
                             new Object[]{"org.jacoco.C", DENY},
                             new Object[]{"org.json.C", DENY},
                             new Object[]{"org.junit.C", DENY},
                             new Object[]{"org.mockito.C", DENY},
                             new Object[]{"org.objenesis.C", DENY},
                             new Object[]{"org.w3c.dom.C", DENY},
                             new Object[]{"org.xml.sax.C", DENY},
                             new Object[]{"org.xmlpull.v1.C", DENY},
                             new Object[]{"foo.R", DENY},
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
    public void accept_should_return_a_value_equal_to_the_expected_value() throws Exception {
        assertSame(expected, testTarget.accept(className));
    }

}

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.support.annotation.AnimatorRes;
import android.support.test.InstrumentationRegistry;

import com.android.internal.util.Predicate;

import junit.framework.TestCase;

import net.bytebuddy.ByteBuddy;

import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.objenesis.Objenesis;
import org.w3c.dom.Attr;
import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlPullParser;

import java.util.Arrays;

import javax.crypto.Cipher;

import dalvik.system.DexFile;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ClassNameFilterImplTest {

    private final ClassNameFilterImpl target = new ClassNameFilterImpl();

    private final String className;
    private final boolean expected;

    public ClassNameFilterImplTest(String className, boolean expected) {
        this.className = className;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "className={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{AnimatorRes.class.getName(), false},
                new Object[]{"android.support.multidex.MultiDex", false},
                new Object[]{InstrumentationRegistry.class.getName(), false},
                new Object[]{Predicate.class.getName(), false},
                new Object[]{"com.github.tmurakami.dexmockito.DexMockitoMockMaker", false},
                new Object[]{DexOpener.class.getName(), false},
                new Object[]{DexFile.class.getName(), false},
                new Object[]{Object.class.getName(), false},
                new Object[]{Cipher.class.getName(), false},
                new Object[]{"libcore.icu.DateIntervalFormat", false},
                new Object[]{TestCase.class.getName(), false},
                new Object[]{ByteBuddy.class.getName(), false},
                new Object[]{"org.apache.harmony.dalvik.NativeTestTarget", false},
                new Object[]{"org.apache.http.Header", false},
                new Object[]{"org.ccil.cowan.tagsoup.AttributesImpl", false},
                new Object[]{CoreMatchers.class.getName(), false},
                new Object[]{JSONObject.class.getName(), false},
                new Object[]{"org.kxml2.io.KXmlParser", false},
                new Object[]{Test.class.getName(), false},
                new Object[]{Mockito.class.getName(), false},
                new Object[]{Objenesis.class.getName(), false},
                new Object[]{Attr.class.getName(), false},
                new Object[]{Attributes.class.getName(), false},
                new Object[]{XmlPullParser.class.getName(), false},
                new Object[]{"sun.misc.Cleaner", false},
                new Object[]{Context.class.getName(), false},
                new Object[]{"foo.bar.R", false},
                new Object[]{"foo.bar.R$string", false},
                new Object[]{"foo.bar.BuildConfig", false},
                new Object[]{"foo.bar.zza", false},
                new Object[]{"foo.bar.Baz$zza", false},
                new Object[]{"foo.bar.Baz$R", true},
                new Object[]{"foo.bar.Baz$BuildConfig", true},
                new Object[]{"foo.bar.Baz", true},
                new Object[]{"android.support.v4.app.Fragment", true});
    }

    @Test
    public void testAccept() {
        assertEquals(expected, target.accept(className));
    }

}

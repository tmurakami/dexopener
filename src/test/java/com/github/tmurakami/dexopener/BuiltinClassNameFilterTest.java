package com.github.tmurakami.dexopener;

import android.app.Application;

import junit.framework.TestCase;

import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Attr;
import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlPullParser;

import java.util.Arrays;

import javax.crypto.Cipher;

import dalvik.system.DexFile;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@RunWith(Parameterized.class)
public class BuiltinClassNameFilterTest {

    @Mock
    ClassNameFilter delegate;

    @InjectMocks
    BuiltinClassNameFilter target;

    private final String name;
    private final boolean expected;

    public BuiltinClassNameFilterTest(String name, boolean expected) {
        this.name = name;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "name={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{Application.class.getName(), false},
                new Object[]{"com.android.dex.ClassData", false},
                new Object[]{DexOpener.class.getName(), false},
                new Object[]{"com.google.android.collect.Lists", false},
                new Object[]{"com.google.android.gles_jni.EGLImpl", false},
                new Object[]{"com.ibm.icu4jni.charset.CharsetDecoderICU", false},
                new Object[]{DexFile.class.getName(), false},
                new Object[]{Object.class.getName(), false},
                new Object[]{Cipher.class.getName(), false},
                new Object[]{TestCase.class.getName(), false},
                new Object[]{"kotlin.Unit", false},
                new Object[]{"kotlinx.Foo", false},
                new Object[]{"libcore.icu.DateIntervalFormat", false},
                new Object[]{"org.apache.commons.logging.Log", false},
                new Object[]{"org.apache.harmony.archive.util.Util", false},
                new Object[]{"org.apache.http.Header", false},
                new Object[]{"org.bouncycastle.asn1.ASN1Choice", false},
                new Object[]{"org.ccil.cowan.tagsoup.AttributesImpl", false},
                new Object[]{CoreMatchers.class.getName(), false},
                new Object[]{JSONObject.class.getName(), false},
                new Object[]{Test.class.getName(), false},
                new Object[]{"org.kxml2.io.KXmlParser", false},
                new Object[]{Attr.class.getName(), false},
                new Object[]{Attributes.class.getName(), false},
                new Object[]{XmlPullParser.class.getName(), false},
                new Object[]{"sun.misc.Cleaner", false},
                new Object[]{"foo.Bar", true});
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAccept() {
        given(delegate.accept(anyString())).willReturn(true);
        assertSame(expected, target.accept(name));
    }

}

package com.github.tmurakami.dexopener;

import android.support.test.InstrumentationRegistry;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Field;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class OpenDexFileTest {

    @Rule
    public TemporaryFolder folder =
            new TemporaryFolder(InstrumentationRegistry.getTargetContext().getCacheDir());

    @SuppressWarnings("deprecation")
    @Test
    public void call_should_generate_the_dex_file() throws Exception {
        ClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                             Modifier.FINAL,
                                             "Ljava/lang/Object;",
                                             null,
                                             null,
                                             Collections.<Annotation>emptySet(),
                                             Collections.<Field>emptySet(),
                                             Collections.<Method>emptySet());
        DexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), Collections.singleton(def));
        File cacheDir = folder.newFolder();
        ClassLoader classLoader = new ClassLoader() {
        };
        Class<?> c = new OpenDexFile(dexFile, cacheDir).call().loadClass("foo.Bar", classLoader);
        assertSame(classLoader, c.getClassLoader());
        assertFalse(Modifier.isFinal(c.getModifiers()));
    }

    @Test(expected = IOException.class)
    public void call_should_throw_IOException_if_the_cache_directory_cannot_be_created()
            throws Exception {
        ClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                             0,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        DexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), Collections.singleton(def));
        new OpenDexFile(dexFile, folder.newFile()).call();
    }

}

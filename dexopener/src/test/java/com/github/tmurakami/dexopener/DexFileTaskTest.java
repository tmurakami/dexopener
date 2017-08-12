package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.rewriter.DexRewriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFileTaskTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private DexRewriter dexRewriter;
    @Mock
    private DexFileLoader dexFileLoader;
    @Mock
    private dalvik.system.DexFile file;

    @Captor
    private ArgumentCaptor<String> tmpPathCaptor;
    @Captor
    private ArgumentCaptor<String> dexPathCaptor;

    @Test
    public void call_should_generate_the_dex_file() throws Exception {
        ClassDef def = new ImmutableClassDef(TypeNameUtils.javaToDexName("foo.Bar"),
                                             0,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        DexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), Collections.singleton(def));
        given(dexRewriter.rewriteDexFile(dexFile)).willReturn(dexFile);
        given(dexFileLoader.loadDex(tmpPathCaptor.capture(), dexPathCaptor.capture(), eq(0))).willReturn(file);
        File cacheDir = folder.newFolder();
        assertSame(file, new DexFileTask(dexFile, dexRewriter, cacheDir, dexFileLoader).call());
        File dex = new File(dexPathCaptor.getValue());
        assertEquals(cacheDir, dex.getParentFile());
        assertTrue(dex.getName().startsWith("classes"));
        assertTrue(dex.getName().endsWith(".dex"));
        assertEquals(dex.getCanonicalPath() + ".tmp.dex", tmpPathCaptor.getValue());
    }

    @Test(expected = IOException.class)
    public void call_should_throw_IOException_if_the_cache_directory_cannot_be_created() throws Exception {
        ClassDef def = new ImmutableClassDef(TypeNameUtils.javaToDexName("foo.Bar"),
                                             0,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        DexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), Collections.singleton(def));
        given(dexRewriter.rewriteDexFile(dexFile)).willReturn(dexFile);
        new DexFileTask(dexFile, dexRewriter, folder.newFile(), dexFileLoader).call();
    }

}

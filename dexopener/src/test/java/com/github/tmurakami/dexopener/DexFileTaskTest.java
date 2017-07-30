package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;

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
    private ClassOpener classOpener;
    @Mock
    private DexFileLoader dexFileLoader;
    @Mock
    private dalvik.system.DexFile dexFile;

    @Captor
    private ArgumentCaptor<String> sourcePathNameCaptor;
    @Captor
    private ArgumentCaptor<String> outputPathNameCaptor;

    @Test
    public void call_should_generate_the_dex_file() throws Exception {
        ClassDef def = new ImmutableClassDef(TypeUtils.getInternalName("foo.Bar"),
                                             0,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        given(classOpener.openClass(def)).willReturn(def);
        given(dexFileLoader.loadDex(sourcePathNameCaptor.capture(),
                                    outputPathNameCaptor.capture(),
                                    eq(0))).willReturn(dexFile);
        File cacheDir = folder.newFolder();
        assertSame(dexFile, new DexFileTask(Opcodes.getDefault(),
                                            Collections.singleton(def),
                                            cacheDir,
                                            classOpener,
                                            dexFileLoader).call());
        File dex = new File(sourcePathNameCaptor.getValue());
        assertEquals(cacheDir, dex.getParentFile());
        assertTrue(dex.getName().startsWith("classes"));
        assertTrue(dex.getName().endsWith(".dex"));
        String outputPathName = outputPathNameCaptor.getValue();
        assertEquals(dex.getCanonicalPath() + ".opt", outputPathName);
    }

    @Test(expected = IOException.class)
    public void call_should_throw_IOException_if_the_cache_directory_cannot_be_created() throws Exception {
        ClassDef def = new ImmutableClassDef(TypeUtils.getInternalName("foo.Bar"),
                                             0,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        given(classOpener.openClass(def)).willReturn(def);
        new DexFileTask(Opcodes.getDefault(),
                        Collections.singleton(def),
                        folder.newFile(),
                        classOpener,
                        dexFileLoader).call();
    }

}
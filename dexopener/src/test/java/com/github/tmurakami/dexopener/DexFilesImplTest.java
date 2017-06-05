package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFilesImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private DexFileLoader dexFileLoader;

    @Mock
    private DexFile dexFile;

    @Captor
    private ArgumentCaptor<String> sourcePathNameCaptor;

    @Captor
    private ArgumentCaptor<String> outputPathNameCaptor;

    @Test
    public void should_generate_the_dex_file_for_the_given_name() throws Exception {
        String className = "foo.Bar";
        ClassDef def = new ImmutableClassDef(TypeUtils.getInternalName(className),
                                             Modifier.FINAL,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        Set<ImmutableDexFile> dexFiles = new HashSet<>();
        dexFiles.add(new ImmutableDexFile(Opcodes.getDefault(),
                                          Collections.singleton(def)));
        Map<String, DexFile> dexFileMap = new HashMap<>();
        given(dexFileLoader.loadDex(sourcePathNameCaptor.capture(),
                                    outputPathNameCaptor.capture(),
                                    eq(0))).willReturn(dexFile);
        given(dexFile.entries())
                .willReturn(Collections.enumeration(Collections.singleton(className)));
        File cacheDir = folder.newFolder();
        assertSame(dexFile, new DexFilesImpl(dexFileMap,
                                             dexFiles,
                                             cacheDir,
                                             dexFileLoader).get(className));
        assertSame(dexFile, dexFileMap.get(className));
        File dex = new File(sourcePathNameCaptor.getValue());
        assertEquals(cacheDir, dex.getParentFile());
        assertTrue(dex.getName().startsWith("classes"));
        assertTrue(dex.getName().endsWith(".dex"));
        String outputPathName = outputPathNameCaptor.getValue();
        assertEquals(dex.getCanonicalPath() + ".opt", outputPathName);
    }

    @Test
    public void should_get_the_mapped_dex_file() throws Exception {
        String className = "foo.Bar";
        Map<String, DexFile> dexFileMap = new HashMap<>();
        dexFileMap.put(className, dexFile);
        assertSame(dexFile, new DexFilesImpl(dexFileMap,
                                             null,
                                             null,
                                             null).get(className));
    }

    @Test
    public void should_get_null_if_the_given_name_is_not_in_the_list_of_dex_files()
            throws Exception {
        assertNull(new DexFilesImpl(Collections.<String, DexFile>emptyMap(),
                                    Collections.<ImmutableDexFile>emptySet(),
                                    null,
                                    null).get("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_cache_dir_cannot_be_created()
            throws Exception {
        String className = "foo.Bar";
        ClassDef def = new ImmutableClassDef(TypeUtils.getInternalName(className),
                                             Modifier.FINAL,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        Set<ImmutableDexFile> dexFiles = new HashSet<>();
        dexFiles.add(new ImmutableDexFile(Opcodes.getDefault(),
                                          Collections.singleton(def)));
        new DexFilesImpl(Collections.<String, DexFile>emptyMap(),
                         dexFiles,
                         folder.newFile(),
                         null).get("foo.Bar");
    }

}

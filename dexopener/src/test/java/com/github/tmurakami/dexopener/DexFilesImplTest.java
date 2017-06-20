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
    public void get_should_generate_the_DexFile_with_the_given_name_if_cache_is_not_hit() throws Exception {
        String className = "foo.Bar";
        ClassDef def = new ImmutableClassDef(TypeUtils.getInternalName(className),
                                             Modifier.FINAL,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        Set<Set<ClassDef>> classesSet = new HashSet<>();
        classesSet.add(Collections.singleton(def));
        Map<String, DexFile> dexFileMap = new HashMap<>();
        given(dexFileLoader.loadDex(sourcePathNameCaptor.capture(),
                                    outputPathNameCaptor.capture(),
                                    eq(0))).willReturn(dexFile);
        given(dexFile.entries()).willReturn(Collections.enumeration(Collections.singleton(className)));
        File cacheDir = folder.newFolder();
        assertSame(dexFile, new DexFilesImpl(Opcodes.getDefault(),
                                             dexFileMap,
                                             classesSet,
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
    public void get_should_return_the_cached_DexFile_with_the_given_name() throws Exception {
        String className = "foo.Bar";
        Map<String, DexFile> dexFileMap = new HashMap<>();
        dexFileMap.put(className, dexFile);
        assertSame(dexFile, new DexFilesImpl(null,
                                             dexFileMap,
                                             null,
                                             null,
                                             null).get(className));
    }

    @Test
    public void get_should_return_null_if_the_given_name_is_not_in_the_list_of_classes_to_be_opened() throws Exception {
        assertNull(new DexFilesImpl(Opcodes.getDefault(),
                                    Collections.<String, DexFile>emptyMap(),
                                    Collections.<Set<ClassDef>>emptySet(),
                                    null,
                                    null).get("foo.Bar"));
    }

    @Test(expected = IOException.class)
    public void get_should_throw_IOException_if_the_cache_directory_cannot_be_created() throws Exception {
        String className = "foo.Bar";
        ClassDef def = new ImmutableClassDef(TypeUtils.getInternalName(className),
                                             Modifier.FINAL,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        Set<Set<ClassDef>> classesSet = new HashSet<>();
        classesSet.add(Collections.singleton(def));
        new DexFilesImpl(Opcodes.getDefault(),
                         Collections.<String, DexFile>emptyMap(),
                         classesSet,
                         folder.newFile(),
                         null).get("foo.Bar");
    }

}

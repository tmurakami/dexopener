package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexClassSourceTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private DexFileLoader dexFileLoader;
    @Mock
    private DexClassFileFactory dexClassFileFactory;
    @Mock
    private dalvik.system.DexFile dexFile;
    @Mock
    private ClassFile classFile;

    @Captor
    private ArgumentCaptor<String> sourcePathNameCaptor;
    @Captor
    private ArgumentCaptor<String> outputPathNameCaptor;

    @Test
    public void getClassFile_should_return_the_ClassFile_with_the_given_name_if_cache_is_missing() throws Exception {
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
        Map<String, dalvik.system.DexFile> dexFileMap = new HashMap<>();
        given(dexFileLoader.loadDex(sourcePathNameCaptor.capture(),
                                    outputPathNameCaptor.capture(),
                                    eq(0))).willReturn(dexFile);
        given(dexFile.entries())
                .willReturn(Collections.enumeration(Collections.singleton(className)));
        given(dexClassFileFactory.newClassFile(className, dexFile)).willReturn(classFile);
        File cacheDir = folder.newFolder();
        assertSame(classFile, new DexClassSource(Opcodes.getDefault(),
                                                 dexFileMap,
                                                 classesSet,
                                                 cacheDir,
                                                 dexFileLoader,
                                                 dexClassFileFactory).getClassFile(className));
        assertSame(dexFile, dexFileMap.get(className));
        File dex = new File(sourcePathNameCaptor.getValue());
        assertEquals(cacheDir, dex.getParentFile());
        assertTrue(dex.getName().startsWith("classes"));
        assertTrue(dex.getName().endsWith(".dex"));
        String outputPathName = outputPathNameCaptor.getValue();
        assertEquals(dex.getCanonicalPath() + ".opt", outputPathName);
    }

    @Test
    public void getClassFile_should_return_the_ClassFile_with_the_given_name_if_cache_is_hit() throws Exception {
        String className = "foo.Bar";
        Map<String, dalvik.system.DexFile> dexFileMap = new HashMap<>();
        dexFileMap.put(className, dexFile);
        given(dexClassFileFactory.newClassFile(className, dexFile)).willReturn(classFile);
        assertSame(classFile, new DexClassSource(null,
                                                 dexFileMap,
                                                 null,
                                                 null,
                                                 null,
                                                 dexClassFileFactory).getClassFile(className));
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_is_not_in_the_list_of_classes_to_be_opened() throws Exception {
        assertNull(new DexClassSource(Opcodes.getDefault(),
                                      Collections.<String, dalvik.system.DexFile>emptyMap(),
                                      Collections.<Set<ClassDef>>emptySet(),
                                      null,
                                      null,
                                      null).getClassFile("foo.Bar"));
    }

    @Test(expected = IOException.class)
    public void getClassFile_should_throw_IOException_if_the_cache_directory_cannot_be_created() throws Exception {
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
        new DexClassSource(Opcodes.getDefault(),
                           Collections.<String, dalvik.system.DexFile>emptyMap(),
                           classesSet,
                           folder.newFile(),
                           null,
                           null).getClassFile("foo.Bar");
    }

}

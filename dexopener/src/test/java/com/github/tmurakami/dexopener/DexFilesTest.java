package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFilesTest {

    @Mock
    private DexFileGenerator dexFileGenerator;
    @Mock
    private DexFile dexFile;

    @Test
    public void should_generate_the_dex_file_for_the_given_name() throws Exception {
        String className = "foo.Bar";
        String internalName = TypeUtils.getInternalName(className);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(ACC_FINAL,
                      internalName,
                      null,
                      TypeUtils.getInternalName(Object.class.getName()),
                      null);
        aw.visitEnd();
        byte[] bytecode = aw.toByteArray();
        given(dexFileGenerator.generateDex(open(bytecode))).willReturn(dexFile);
        given(dexFile.entries()).willReturn(Collections.enumeration(Collections.singleton(className)));
        Set<Set<String>> internalNamesSet = new HashSet<>();
        internalNamesSet.add(Collections.singleton(internalName));
        Map<String, DexFile> dexFileMap = new HashMap<>();
        assertSame(dexFile, new DexFiles(bytecode,
                                         dexFileMap,
                                         internalNamesSet,
                                         dexFileGenerator).get(className));
        assertTrue(internalNamesSet.isEmpty());
        assertSame(dexFile, dexFileMap.get(className));
    }

    @Test
    public void should_get_the_mapped_dex_file() throws Exception {
        String className = "foo.Bar";
        Map<String, DexFile> dexFileMap = new HashMap<>();
        dexFileMap.put(className, dexFile);
        assertSame(dexFile, new DexFiles(null,
                                         dexFileMap,
                                         null,
                                         null).get(className));
    }

    @Test
    public void should_get_null_if_the_bytecode_is_null() throws Exception {
        assertNull(new DexFiles(null,
                                Collections.<String, DexFile>emptyMap(),
                                Collections.<Set<String>>emptySet(),
                                null).get("foo.Bar"));
    }

    @Test
    public void should_get_null_if_the_given_name_is_not_in_the_list_of_internal_names()
            throws Exception {
        assertNull(new DexFiles(new byte[0],
                                Collections.<String, DexFile>emptyMap(),
                                Collections.<Set<String>>emptySet(),
                                null).get("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_class_for_the_given_name_cannot_be_found()
            throws Exception {
        String className = "foo.Bar";
        String internalName = TypeUtils.getInternalName(className);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitEnd();
        Set<Set<String>> internalNamesSet = new HashSet<>();
        internalNamesSet.add(Collections.singleton(internalName));
        DexFiles testTarget = new DexFiles(aw.toByteArray(),
                                           Collections.<String, DexFile>emptyMap(),
                                           internalNamesSet,
                                           null);
        try {
            testTarget.get(className);
        } finally {
            assertTrue(internalNamesSet.isEmpty());
        }
    }

    private static byte[] open(byte[] bytecode) {
        ApplicationWriter aw = new ApplicationWriter();
        new ApplicationReader(ASM4, bytecode).accept(new ApplicationOpener(aw), null, 0);
        return aw.toByteArray();
    }

}

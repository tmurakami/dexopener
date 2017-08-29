package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexClassSourceTest {

    @Mock
    private DexClassFileFactory dexClassFileFactory;
    @Mock
    private DexFileHolder dexFileHolder;
    @Mock
    private dalvik.system.DexFile dexFile;
    @Mock
    private ClassFile classFile;

    @Test
    public void getClassFile_should_return_the_ClassFile_if_the_given_name_is_in_the_map_of_holders() throws Exception {
        String className = "foo.Bar";
        given(dexFileHolder.get()).willReturn(dexFile);
        given(dexClassFileFactory.newClassFile(className, dexFile)).willReturn(classFile);
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        holderMap.put(className, dexFileHolder);
        assertSame(classFile, new DexClassSource(holderMap, dexClassFileFactory).getClassFile(className));
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_is_not_in_the_map_of_holders() throws Exception {
        assertNull(new DexClassSource(Collections.<String, DexFileHolder>emptyMap(),
                                      dexClassFileFactory).getClassFile("foo.Bar"));
    }

}

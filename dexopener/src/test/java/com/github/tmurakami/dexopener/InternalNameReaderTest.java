package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InternalNameReaderTest {

    @InjectMocks
    private InternalNameReader testTarget;

    @Mock
    private ClassNameFilter filter;

    @Test
    public void should_get_only_class_names_that_passed_through_the_filter() throws Exception {
        String className = "foo.Bar";
        String internalName = DexUtils.toInternalName(className);
        given(filter.accept(className)).willReturn(true);
        ApplicationWriter aw = new ApplicationWriter();
        String superInternalName = DexUtils.toInternalName(Object.class.getName());
        aw.visitClass(ACC_FINAL, internalName, null, superInternalName, null);
        aw.visitClass(ACC_FINAL,
                      DexUtils.toInternalName("foo.bar.Baz"),
                      null,
                      superInternalName,
                      null);
        aw.visitEnd();
        ApplicationReader ar = new ApplicationReader(ASM4, aw.toByteArray());
        assertEquals(Collections.singleton(internalName), testTarget.read(ar));
    }

}

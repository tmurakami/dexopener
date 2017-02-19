package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassNameReaderTest {

    @InjectMocks
    ClassNameReader testTarget;

    @Mock
    ClassNameFilter filter;

    @Test
    public void the_readClassNames_method_should_read_class_names_that_pass_the_ClassNameFilter() throws Exception {
        given(filter.accept("foo.Bar")).willReturn(true);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0, "Lfoo/Bar;", null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        byte[] bytes = aw.toByteArray();
        Set<String> classNames = testTarget.readClassNames(new ApplicationReader(ASM4, bytes));
        assertEquals(1, classNames.size());
        assertEquals("foo.Bar", classNames.iterator().next());
    }

    @Test
    public void the_readClassNames_method_should_not_read_class_names_that_do_not_pass_the_ClassNameFilter() throws Exception {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0, "Lfoo/Bar;", null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        byte[] bytes = aw.toByteArray();
        assertTrue(testTarget.readClassNames(new ApplicationReader(ASM4, bytes)).isEmpty());
    }

}

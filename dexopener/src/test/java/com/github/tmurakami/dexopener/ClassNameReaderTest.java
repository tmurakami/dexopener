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
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClassNameReaderTest {

    @InjectMocks
    private ClassNameReader testTarget;

    @Mock
    private ClassNameFilter filter;

    @Test
    public void readClassNames_should_return_only_class_names_that_passed_through_the_filter()
            throws Exception {
        given(filter.accept("foo.Bar")).willReturn(true);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0,
                      "Lfoo/Bar;",
                      null,
                      "Ljava/lang/Object;",
                      null);
        aw.visitClass(0,
                      "Lfoo/bar/Baz;",
                      null,
                      "Ljava/lang/Object;",
                      null);
        aw.visitEnd();
        byte[] byteCode = aw.toByteArray();
        Set<String> classNames = testTarget.readClassNames(new ApplicationReader(ASM4, byteCode));
        assertEquals(1, classNames.size());
        assertEquals("foo.Bar", classNames.iterator().next());
    }

}

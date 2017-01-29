package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

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
    public void visitClass() throws Exception {
        testTarget.visitClass(0, "Lfoo/Bar;", null, null, null);
        Set<String> classNames = testTarget.getClassNames();
        assertTrue(classNames.isEmpty());
    }

    @Test
    public void visitClass_matched() throws Exception {
        given(filter.accept("foo.Bar")).willReturn(true);
        testTarget.visitClass(0, "Lfoo/Bar;", null, null, null);
        Set<String> classNames = testTarget.getClassNames();
        assertEquals(1, classNames.size());
        assertEquals("foo.Bar", classNames.iterator().next());
    }

    @Test
    public void getClassNames() throws Exception {
        given(filter.accept("foo.Bar")).willReturn(true);
        testTarget.visitClass(0, "Lfoo/Bar;", null, null, null);
        testTarget.getClassNames();
        assertTrue(testTarget.getClassNames().isEmpty());
    }

}
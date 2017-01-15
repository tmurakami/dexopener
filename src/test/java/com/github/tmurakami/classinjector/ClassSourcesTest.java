package com.github.tmurakami.classinjector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassSourcesTest {

    @Mock
    ClassSource source;

    @Test(expected = IllegalArgumentException.class)
    public void _new_nullSources() {
        new ClassSources(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void _new_containsNullSource() {
        new ClassSources(Collections.singleton((ClassSource) null));
    }

    @Test
    public void getBytecodeFor() throws IOException {
        byte[] bytecode = {};
        given(source.getBytecodeFor("foo.Bar")).willReturn(bytecode);
        assertSame(bytecode, new ClassSources(Collections.singleton(source)).getBytecodeFor("foo.Bar"));
    }

    @Test
    public void getBytecodeFor_bytecodeNotFound() throws IOException {
        assertNull(new ClassSources(Collections.singleton(source)).getBytecodeFor("foo.Bar"));
    }

}

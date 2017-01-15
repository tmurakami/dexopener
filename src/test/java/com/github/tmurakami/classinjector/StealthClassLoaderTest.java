package com.github.tmurakami.classinjector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOError;
import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class StealthClassLoaderTest {

    @Mock
    ClassDefiner definer;
    @Mock
    ClassSource source;
    @Mock
    ClassLoader injectionTarget;

    @InjectMocks
    StealthClassLoader testTarget;

    @Test
    public void findClass() throws IOException, ClassNotFoundException {
        byte[] bytecode = {};
        given(source.getBytecodeFor("foo.Bar")).willReturn(bytecode);
        Class<?> c = getClass();
        given(definer.defineClass("foo.Bar", bytecode, injectionTarget)).willReturn(c);
        assertSame(c, testTarget.findClass("foo.Bar"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void findClass_findMyClass() throws ClassNotFoundException {
        testTarget.findClass(StealthClassLoaderTest.class.getName());
    }

    @Test(expected = ClassNotFoundException.class)
    public void findClass_bytecodeNotFound() throws IOException, ClassNotFoundException {
        testTarget.findClass("foo.Bar");
        then(source).should().getBytecodeFor("foo.Bar");
    }

    @Test(expected = IOError.class)
    public void findClass_ioExceptionWhileGettingBytecode() throws IOException, ClassNotFoundException {
        given(source.getBytecodeFor("foo.Bar")).willThrow(IOException.class);
        testTarget.findClass("foo.Bar");
    }

    @Test(expected = NoClassDefFoundError.class)
    public void findClass_nullClass() throws IOException, ClassNotFoundException {
        byte[] bytecode = {};
        given(source.getBytecodeFor("foo.Bar")).willReturn(bytecode);
        testTarget.findClass("foo.Bar");
    }

}

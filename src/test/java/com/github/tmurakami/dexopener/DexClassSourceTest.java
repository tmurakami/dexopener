package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexClassSourceTest {

    @Mock
    File cacheDir;
    @Mock
    DexBytesClassFileFactory classFileFactory;
    @Mock
    ClassFile classFile;
    @Mock
    ApplicationReader applicationReader;


    @Test
    public void getClassFile() throws Exception {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0, "Lfoo/Bar;", null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        byte[] bytes = aw.toByteArray();
        given(classFileFactory.create(eq("foo.Bar"), aryEq(bytes), eq(cacheDir))).willReturn(classFile);
        ApplicationReader ar = new ApplicationReader(ASM4, bytes);
        assertSame(classFile, new DexClassSource(ar, cacheDir, classFileFactory).getClassFile("foo.Bar"));
    }

    @Test
    public void getClassFile_nullBytes() throws Exception {
        assertNull(new DexClassSource(applicationReader, cacheDir, classFileFactory).getClassFile("foo.Bar"));
    }

}

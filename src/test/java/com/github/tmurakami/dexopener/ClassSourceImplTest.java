package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassSourceImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    ClassNameFilter classNameFilter;
    @Mock
    DexClassSourceFactory dexClassSourceFactory;
    @Mock
    ClassSource classSource;
    @Mock
    ClassFile classFile;

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Test
    public void getClassFile() throws Exception {
        File apk = folder.newFile();
        byte[] bytes = "abc".getBytes();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(apk));
        try {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(bytes);
        } finally {
            out.close();
        }
        given(classNameFilter.accept("foo.Bar")).willReturn(true);
        given(dexClassSourceFactory.newClassSource(bytes)).willReturn(classSource);
        given(classSource.getClassFile("foo.Bar")).willReturn(classFile);
        String sourceDir = apk.getCanonicalPath();
        ClassSourceImpl testTarget = new ClassSourceImpl(sourceDir, classNameFilter, dexClassSourceFactory);
        assertSame(classFile, testTarget.getClassFile("foo.Bar"));
    }

    @Test
    public void getClassFile_classNameNotAccepted() throws Exception {
        assertNull(new ClassSourceImpl("", classNameFilter, dexClassSourceFactory).getClassFile("foo.Bar"));
    }

}

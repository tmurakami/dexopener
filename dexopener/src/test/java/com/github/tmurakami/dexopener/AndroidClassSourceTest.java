package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.classinjector.ClassSource;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AndroidClassSourceTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private ClassNameFilter classNameFilter;
    @Mock
    private DexClassSourceFactory dexClassSourceFactory;
    @Mock
    private ClassSource classSource;
    @Mock
    private ClassFile classFile;

    @Captor
    private ArgumentCaptor<byte[]> byteCodeCaptor;
    @Captor
    private ArgumentCaptor<Set<String>> classNamesCaptor;

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Test
    public void getClassFile_should_return_the_ClassFile_with_the_given_name() throws Exception {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0,
                      "Lfoo/Bar;",
                      null,
                      "Ljava/lang/Object;",
                      null);
        aw.visitEnd();
        byte[] byteCode = aw.toByteArray();
        File apk = folder.newFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(apk));
        try {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(byteCode);
        } finally {
            out.close();
        }
        given(classNameFilter.accept("foo.Bar")).willReturn(true);
        given(dexClassSourceFactory.newClassSource(byteCodeCaptor.capture(),
                                                   classNamesCaptor.capture()))
                .willReturn(classSource);
        given(classSource.getClassFile("foo.Bar")).willReturn(classFile);
        AndroidClassSource testTarget = new AndroidClassSource(apk.getCanonicalPath(),
                                                               classNameFilter,
                                                               dexClassSourceFactory);
        assertSame(classFile, testTarget.getClassFile("foo.Bar"));
        assertArrayEquals(byteCode, byteCodeCaptor.getValue());
        Set<String> classNames = classNamesCaptor.getValue();
        assertEquals(1, classNames.size());
        assertEquals("foo.Bar", classNames.iterator().next());
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_does_not_pass_through_the_filter()
            throws Exception {
        AndroidClassSource classSource = new AndroidClassSource("",
                                                                classNameFilter,
                                                                dexClassSourceFactory);
        assertNull(classSource.getClassFile("foo.Bar"));
    }

}

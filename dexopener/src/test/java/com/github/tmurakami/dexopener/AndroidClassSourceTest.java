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
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
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
    private DexFilesFactory dexFilesFactory;
    @Mock
    private DexClassSourceFactory dexClassSourceFactory;
    @Mock
    private DexFiles dexFiles;
    @Mock
    private ClassSource classSource;
    @Mock
    private ClassFile classFile;

    @Captor
    private ArgumentCaptor<byte[]> bytecodeCaptor;
    @Captor
    private ArgumentCaptor<Set<String>> classNamesCaptor;

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Test
    public void should_get_the_class_file_for_the_given_name() throws Exception {
        String className = "foo.Bar";
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(ACC_FINAL,
                      TypeUtils.getInternalName(className),
                      null,
                      TypeUtils.getInternalName(Object.class.getName()),
                      null);
        aw.visitEnd();
        byte[] bytecode = aw.toByteArray();
        File apk = folder.newFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(apk));
        try {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(bytecode);
        } finally {
            out.close();
        }
        given(classNameFilter.accept(className)).willReturn(true);
        given(dexFilesFactory.newDexFiles(bytecodeCaptor.capture(),
                                          classNamesCaptor.capture())).willReturn(dexFiles);
        given(dexClassSourceFactory.newClassSource(dexFiles)).willReturn(classSource);
        given(classSource.getClassFile(className)).willReturn(classFile);
        assertSame(classFile, new AndroidClassSource(apk.getCanonicalPath(),
                                                     classNameFilter,
                                                     dexFilesFactory,
                                                     dexClassSourceFactory).getClassFile(className));
        assertArrayEquals(bytecode, bytecodeCaptor.getValue());
        assertEquals(Collections.singleton(className), classNamesCaptor.getValue());
    }

    @Test
    public void should_get_null_if_the_given_name_does_not_pass_through_the_filter()
            throws Exception {
        AndroidClassSource classSource = new AndroidClassSource("",
                                                                classNameFilter,
                                                                dexFilesFactory, dexClassSourceFactory);
        assertNull(classSource.getClassFile("foo.Bar"));
    }

}

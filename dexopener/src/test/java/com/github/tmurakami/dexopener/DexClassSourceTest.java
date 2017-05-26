package com.github.tmurakami.dexopener;

import com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexClassSourceTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private File cacheDir;
    @Mock
    private DexFileLoader dexFileLoader;
    @Mock
    private DexClassFileFactory classFileFactory;
    @Mock
    private DexFile dexFile;
    @Mock
    private ClassFile classFile;

    @Test
    public void should_get_a_ClassFile_with_the_given_name() throws Exception {
        String className = "foo.Bar";
        String internalName = DexUtils.toInternalName(className);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0,
                      internalName,
                      null,
                      DexUtils.toInternalName(Object.class.getName()),
                      null);
        aw.visitEnd();
        final byte[] byteCode = aw.toByteArray();
        final File cacheDir = folder.newFolder();
        given(dexFileLoader.loadDex(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                File f = new File(argument);
                String name = f.getName();
                return cacheDir.equals(f.getParentFile())
                        && name.startsWith("classes")
                        && name.endsWith(".zip")
                        && Arrays.equals(byteCode, readByteCode(f));
            }
        }), argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                File f = new File(argument);
                String name = f.getName();
                return cacheDir.equals(f.getParentFile())
                        && name.startsWith("classes")
                        && name.endsWith(".zip.dex");
            }
        }), eq(0))).willReturn(dexFile);
        given(classFileFactory.newClassFile(className, dexFile)).willReturn(classFile);
        DexClassSource source = new DexClassSource(byteCode,
                                                   Collections.singleton(Collections.singleton(internalName)),
                                                   cacheDir,
                                                   dexFileLoader,
                                                   classFileFactory);
        assertSame(classFile, source.getClassFile(className));
    }

    @Test
    public void should_get_null_if_the_given_name_is_not_in_the_list_of_class_names()
            throws Exception {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitEnd();
        DexClassSource classSource = new DexClassSource(aw.toByteArray(),
                                                        Collections.<Set<String>>emptySet(),
                                                        cacheDir,
                                                        dexFileLoader,
                                                        classFileFactory);
        assertNull(classSource.getClassFile("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_class_for_the_given_name_cannot_be_found()
            throws Exception {
        String className = "foo.Bar";
        String internalName = DexUtils.toInternalName(className);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitEnd();
        new DexClassSource(aw.toByteArray(),
                           Collections.singleton(Collections.singleton(internalName)),
                           cacheDir,
                           dexFileLoader,
                           classFileFactory).getClassFile(className);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_IllegalStateException_if_the_cache_dir_cannot_be_created()
            throws Exception {
        String className = "foo.Bar";
        String internalName = DexUtils.toInternalName(className);
        ApplicationWriter aw = new ApplicationWriter();
        aw.visitClass(0,
                      internalName,
                      null,
                      DexUtils.toInternalName(Object.class.getName()),
                      null);
        aw.visitEnd();
        new DexClassSource(aw.toByteArray(),
                           Collections.singleton(Collections.singleton(internalName)),
                           cacheDir,
                           dexFileLoader,
                           classFileFactory).getClassFile(className);
    }

    private static byte[] readByteCode(File zip) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zip);
            ZipEntry e = zipFile.getEntry("classes.dex");
            return IOUtils.readBytes(zipFile.getInputStream(e));
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}

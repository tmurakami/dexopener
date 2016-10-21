package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexFile;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_PRIVATE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_STATIC;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DexElementImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    ClassNameFilter classNameFilter;
    @Mock
    DexFileLoader fileLoader;
    @Mock
    DexFile dexFile;
    @Mock
    ClassLoader classLoader;

    @Test
    public void testLoadClass() throws IOException {
        given(classNameFilter.accept(C.class.getName())).willReturn(true);
        final File cacheDir = folder.newFolder();
        given(fileLoader.load(
                argThat(new ArgumentMatcher<String>() {
                    @Override
                    public boolean matches(String path) {
                        File file = new File(path);
                        return path.endsWith(".zip")
                                && file.exists()
                                && file.getName().startsWith("classes")
                                && file.getParentFile().equals(cacheDir);
                    }
                }),
                argThat(new ArgumentMatcher<String>() {
                    @Override
                    public boolean matches(String path) {
                        File file = new File(path);
                        return path.endsWith(".zip.dex")
                                && !file.exists()
                                && file.getName().startsWith("classes")
                                && file.getParentFile().equals(cacheDir);
                    }
                }))).willReturn(dexFile);
        given(dexFile.loadClass(C.class.getName(), classLoader)).willReturn(C.class);
        ApplicationReader ar = new ApplicationReader(ASM4, generateBytes());
        assertSame(C.class, new DexElementImpl(ar, cacheDir, classNameFilter, fileLoader).loadClass(C.class.getName(), classLoader));
    }

    private static byte[] generateBytes() {
        String name = 'L' + C.class.getName().replace('.', '/') + ';';
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        aw.visitClass(ACC_PRIVATE | ACC_STATIC, name, null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        return aw.toByteArray();
    }

    private static class C {
    }

}

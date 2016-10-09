package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ClassVisitor;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.MethodVisitor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.io.IOUtil;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_CODE;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationReader.SKIP_DEBUG;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ACC_FINAL;
import static com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.Opcodes.ASM4;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassesJarGeneratorImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    ClassNameFilter classNameFilter;

    @InjectMocks
    ClassesJarGeneratorImpl target;

    @Test
    public void testGenerateClassesJar() throws IOException {
        String name = getClass().getName() + "$C";
        final String internalName = 'L' + name.replace('.', '/') + ';';
        given(classNameFilter.accept(name)).willReturn(true);
        String apkPath = generateClassesJar(internalName);
        String jarPath = target.generateClassesJar(apkPath, folder.newFolder());
        JarInputStream in = new JarInputStream(new FileInputStream(jarPath));
        try {
            ZipEntry e = in.getNextEntry();
            assertEquals("classes.dex", e.getName());
            new ApplicationReader(ASM4, in).accept(new ApplicationVisitor(ASM4) {
                @Override
                public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
                    assertEquals(internalName, name);
                    assertEquals(0, access & ACC_FINAL);
                    return new ClassVisitor(ASM4, super.visitClass(access, name, signature, superName, interfaces)) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
                            assertEquals("foo", name);
                            assertEquals(0, access & ACC_FINAL);
                            return null;
                        }
                    };
                }
            }, SKIP_CODE | SKIP_DEBUG);
        } finally {
            IOUtil.closeQuietly(in);
        }
    }

    private String generateClassesJar(String internalName) throws IOException {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        ClassVisitor cv = aw.visitClass(ACC_FINAL, internalName, null, "Ljava/lang/Object;", null);
        cv.visitMethod(ACC_FINAL, "foo", "V", null, null);
        aw.visitEnd();
        File file = folder.newFile();
        JarOutputStream out = new JarOutputStream(new FileOutputStream(file));
        try {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(aw.toByteArray());
            out.closeEntry();
        } finally {
            IOUtil.closeQuietly(out);
        }
        return file.getCanonicalPath();
    }

}

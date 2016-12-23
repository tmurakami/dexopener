package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class InstallerImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    DexElementFactory elementFactory;
    @Mock
    ClassLoaderFactory classLoaderFactory;
    @Mock
    ClassLoaderHelper classLoaderHelper;
    @Mock
    Context context;
    @Mock
    ClassLoader classLoader;
    @Mock
    ClassLoader newClassLoader;

    @InjectMocks
    InstallerImpl target;

    @Test
    public void testInstall() throws IOException {
        ApplicationInfo ai = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(ai);
        int size = 3;
        List<byte[]> bytes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            bytes.add(generateDexBytes(toInternalName(getClass().getName() + "$C" + i)));
        }
        File zip = createClassesZip(folder, bytes);
        ai.sourceDir = zip.getCanonicalPath();
        File dataDir = folder.newFolder();
        ai.dataDir = dataDir.getCanonicalPath();
        File cacheDir = new File(dataDir, "code_cache/dexopener");
        List<DexElement> elements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            DexElement element = mock(DexElement.class);
            elements.add(element);
            final byte[] expected = bytes.get(i);
            given(elementFactory.newDexElement(argThat(new ArgumentMatcher<byte[]>() {
                @Override
                public boolean matches(byte[] argument) {
                    return Arrays.equals(expected, argument);
                }
            }), eq(cacheDir))).willReturn(element);
        }
        given(context.getClassLoader()).willReturn(classLoader);
        given(classLoaderFactory.newClassLoader(classLoader, elements)).willReturn(newClassLoader);
        target.install(context);
        then(classLoaderHelper).should().setParent(classLoader, newClassLoader);
    }

    private static String toInternalName(String name) {
        return 'L' + name.replace('.', '/') + ';';
    }

    private static byte[] generateDexBytes(String name) {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        aw.visitClass(0, name, null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        return aw.toByteArray();
    }

    private static File createClassesZip(TemporaryFolder folder, List<byte[]> bytes) throws IOException {
        File file = folder.newFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        try {
            int size = bytes.size();
            for (int i = 0; i < size; i++) {
                out.putNextEntry(new ZipEntry("classes" + (i == 0 ? "" : (i + 1)) + ".dex"));
                out.write(bytes.get(i));
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
        return file;
    }

}

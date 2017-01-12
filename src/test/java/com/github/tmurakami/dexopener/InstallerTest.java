package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class InstallerTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    MultiDexHelper multiDexHelper;
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
    Installer target;

    @Test
    public void testInstall() throws IOException {
        ApplicationInfo ai = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(ai);
        ai.sourceDir = "apk";
        ai.dataDir = folder.getRoot().getCanonicalPath();
        File cacheDir = folder.newFolder("code_cache", "dexopener");
        DexElement element = mock(DexElement.class);
        given(elementFactory.newDexElement(new File("apk"), cacheDir)).willReturn(element);
        folder.newFolder("code_cache", "secondary-dexes");
        DexElement element2 = mock(DexElement.class);
        given(elementFactory.newDexElement(folder.newFile("code_cache/secondary-dexes/apk.classes2.zip"), cacheDir)).willReturn(element2);
        given(context.getClassLoader()).willReturn(classLoader);
        given(classLoaderFactory.newClassLoader(classLoader, Arrays.asList(element, element2))).willReturn(newClassLoader);
        target.install(context);
        InOrder inOrder = inOrder(multiDexHelper, context, classLoaderHelper);
        then(multiDexHelper).should(inOrder).install(context);
        then(context).should(inOrder).getApplicationInfo();
        then(classLoaderHelper).should(inOrder).setParent(classLoader, newClassLoader);
    }

}

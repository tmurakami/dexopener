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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DexInstallerImplTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    MultiDexHelper multiDexHelper;
    @Mock
    DexFactory dexFactory;
    @Mock
    ClassLoaderFactory classLoaderFactory;
    @Mock
    ClassLoaderHelper classLoaderHelper;
    @Mock
    Context context;
    @Mock
    ClassLoader classLoader;

    @InjectMocks
    DexInstallerImpl target;

    @Test
    public void testInstall() throws IOException {
        ApplicationInfo ai = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(ai);
        ai.sourceDir = "apk";
        File cacheDir = new File("cacheDir");
        given(context.getDir("dexopener", Context.MODE_PRIVATE)).willReturn(cacheDir);
        Dex dex = mock(Dex.class);
        given(dexFactory.newDex(new File("apk"), cacheDir)).willReturn(dex);
        ai.dataDir = folder.getRoot().getCanonicalPath();
        folder.newFolder("code_cache", "secondary-dexes");
        Dex dex2 = mock(Dex.class);
        given(dexFactory.newDex(folder.newFile("code_cache/secondary-dexes/apk.classes2.zip"), cacheDir)).willReturn(dex2);
        ClassLoader loader = mock(ClassLoader.class);
        given(context.getClassLoader()).willReturn(loader);
        given(classLoaderFactory.newClassLoader(loader, Arrays.asList(dex, dex2))).willReturn(classLoader);
        target.install(context);
        InOrder inOrder = inOrder(multiDexHelper, context, classLoaderHelper);
        then(multiDexHelper).should(inOrder).installMultiDex(context);
        then(context).should(inOrder).getApplicationInfo();
        then(classLoaderHelper).should(inOrder).setParent(loader, classLoader);
    }

}

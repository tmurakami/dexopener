package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DexInstallerImplTest {

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
    SharedPreferences prefs;
    @Mock
    ClassLoader classLoader;

    @InjectMocks
    DexInstallerImpl target;

    @Test
    public void testInstall() {
        given(context.getSharedPreferences("multidex.version", Context.MODE_PRIVATE)).willReturn(prefs);
        given(prefs.getInt("dex.number", 1)).willReturn(2);
        ApplicationInfo ai = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(ai);
        ai.sourceDir = "path/to/apk";
        File cacheDir = new File("path/to/cacheDir");
        given(context.getDir("dexopener", Context.MODE_PRIVATE)).willReturn(cacheDir);
        Dex dex = mock(Dex.class);
        given(dexFactory.newDex(new File("path/to/apk"), cacheDir)).willReturn(dex);
        ai.dataDir = "path/to/dataDir";
        Dex dex2 = mock(Dex.class);
        given(dexFactory.newDex(new File("path/to/dataDir/code_cache/secondary-dexes/apk.classes2.zip"), cacheDir)).willReturn(dex2);
        ClassLoader loader = mock(ClassLoader.class);
        given(context.getClassLoader()).willReturn(loader);
        given(classLoaderFactory.newClassLoader(loader, Arrays.asList(dex, dex2))).willReturn(classLoader);
        target.install(context);
        InOrder inOrder = inOrder(multiDexHelper, context, classLoaderHelper);
        then(multiDexHelper).should(inOrder).installMultiDex(context);
        then(context).should(inOrder).getSharedPreferences(anyString(), anyInt());
        then(classLoaderHelper).should(inOrder).setParent(loader, classLoader);
    }

}

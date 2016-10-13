package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import dalvik.system.DexFile;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class DexInstallerImplTest {

    @Mock
    MultiDex multiDex;
    @Mock
    DexConverter dexConverter;
    @Mock
    DexFileLoader dexFileLoader;
    @Mock
    ClassLoaderInstaller classLoaderInstaller;
    @Mock
    Context context;
    @Mock
    SharedPreferences prefs;
    @Mock
    ClassLoader classLoader;

    @InjectMocks
    DexInstallerImpl target;

    @Test
    public void testInstall() throws IOException {
        File cacheDir = new File("/dexopener");
        given(context.getDir("dexopener", Context.MODE_PRIVATE)).willReturn(cacheDir);
        given(context.getSharedPreferences("multidex.version", Context.MODE_PRIVATE)).willReturn(prefs);
        given(prefs.getInt("dex.number", 1)).willReturn(2);
        ApplicationInfo ai = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(ai);
        ai.sourceDir = "apk";
        given(dexConverter.convert(new File("apk"), cacheDir)).willReturn(new File("/zip"));
        DexFile dexFile = new DexFile("dexFile");
        given(dexFileLoader.load("/zip", "/dexopener/zip.dex")).willReturn(dexFile);
        ai.dataDir = "/data";
        given(dexConverter.convert(new File("/data/code_cache/secondary-dexes/apk.classes2.zip"), cacheDir)).willReturn(new File("/zip2"));
        DexFile dexFile2 = new DexFile("dexFile2");
        given(dexFileLoader.load("/zip2", "/dexopener/zip2.dex")).willReturn(dexFile2);
        given(context.getClassLoader()).willReturn(classLoader);
        target.install(context);
        then(classLoaderInstaller).should().install(classLoader, Arrays.asList(dexFile, dexFile2));
    }

}

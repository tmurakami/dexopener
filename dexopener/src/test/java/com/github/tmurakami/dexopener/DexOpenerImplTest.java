package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class DexOpenerImplTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @InjectMocks
    private DexOpenerImpl testTarget;

    @Mock
    private Context context;
    @Mock
    private AndroidClassSourceFactory androidClassSourceFactory;
    @Mock
    private ClassInjectorFactory classInjectorFactory;
    @Mock
    private ClassSource classSource;
    @Mock
    private ClassInjector classInjector;

    @Test
    public void installTo_should_inject_the_class_source_into_the_given_class_loader() throws Exception {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(applicationInfo);
        applicationInfo.sourceDir = "test";
        String dataDir = folder.newFolder().getCanonicalPath();
        applicationInfo.dataDir = dataDir;
        File cacheDir = new File(dataDir, "code_cache/dexopener");
        given(androidClassSourceFactory.newClassSource("test", cacheDir)).willReturn(classSource);
        given(classInjectorFactory.newClassInjector(classSource)).willReturn(classInjector);
        ClassLoader classLoader = new ClassLoader() {
        };
        testTarget.installTo(classLoader);
        then(classInjector).should().into(classLoader);
    }

    @Test(expected = IllegalStateException.class)
    public void installTo_should_throw_IllegalStateException_if_the_Application_has_been_created() throws Exception {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(applicationInfo);
        given(context.getApplicationContext()).willReturn(new Application());
        ClassLoader classLoader = new ClassLoader() {
        };
        testTarget.installTo(classLoader);
        then(classInjector).should(never()).into(classLoader);
    }

}

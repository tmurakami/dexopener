package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassFile;
import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

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
    private ClassSource classSource;
    @Mock
    private ClassFile classFile;

    @Test
    public void installTo_should_inject_the_class_source_into_the_given_class_loader() throws Exception {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(applicationInfo);
        applicationInfo.sourceDir = "test";
        String dataDir = folder.newFolder().getCanonicalPath();
        applicationInfo.dataDir = dataDir;
        File cacheDir = new File(dataDir, "code_cache/dexopener");
        given(androidClassSourceFactory.newClassSource("test", cacheDir)).willReturn(classSource);
        given(classSource.getClassFile("foo.Bar")).willReturn(classFile);
        ClassLoader classLoader = new ClassLoader() {
        };
        given(classFile.toClass(classLoader)).willReturn(getClass());
        testTarget.installTo(classLoader);
        assertSame(getClass(), classLoader.loadClass("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void installTo_should_throw_IllegalStateException_if_the_Application_has_been_created() {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(applicationInfo);
        given(context.getApplicationContext()).willReturn(new Application());
        ClassLoader classLoader = new ClassLoader() {
        };
        testTarget.installTo(classLoader);
    }

}

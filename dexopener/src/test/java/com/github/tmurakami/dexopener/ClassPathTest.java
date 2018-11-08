/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.tmurakami.dexopener.repackaged.com.google.common.base.Predicate;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import test.MyClass;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClassPathTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock(stubOnly = true)
    private Context context;
    @Mock(stubOnly = true)
    private Predicate<String> classNameFilter;
    @Mock(stubOnly = true)
    private DexFileLoader dexFileLoader;
    @Mock
    private Executor executor;

    @Test
    public void should_get_null_if_the_given_name_does_not_pass_through_the_class_name_filter() {
        ClassPath classPath = new ClassPath(context, classNameFilter, dexFileLoader, executor);
        assertNull(classPath.loadClass("foo.Bar", null));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void should_get_the_Class_with_the_given_name() throws IOException {
        given(classNameFilter.apply(anyString())).willReturn(true);
        ApplicationInfo ai = new ApplicationInfo();
        given(context.getApplicationInfo()).willReturn(ai);
        ai.dataDir = folder.newFolder().getCanonicalPath();
        int classCount = 101; // MAX_CLASSES_PER_DEX_FILE + 1
        List<String> classNames = new ArrayList<>(classCount);
        for (int i = 0; i < classCount; i++) {
            classNames.add("foo.Bar" + i);
        }
        Set<ImmutableClassDef> classes = new HashSet<>();
        for (String className : classNames) {
            classes.add(new ImmutableClassDef('L' + className.replace('.', '/') + ';', 0,
                                              null, null, null, null, null, null));
        }
        ImmutableDexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), classes);
        File zip = folder.newFile();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(DexPoolUtils.toBytecode(dexFile));
        }
        ai.sourceDir = zip.getCanonicalPath();
        ClassLoader loader = new ClassLoader() {
        };
        given(dexFileLoader.loadDex(anyString(), anyString()))
                .will(answer((src, out) -> {
                    dalvik.system.DexFile file = mock(dalvik.system.DexFile.class,
                                                      withSettings().stubOnly());
                    given(file.loadClass(anyString(), eq(loader))).willReturn(MyClass.class);
                    return file;
                }));
        ClassPath classPath = new ClassPath(context, classNameFilter, dexFileLoader, executor);
        for (String className : classNames) {
            assertSame(MyClass.class, classPath.loadClass(className, loader));
        }
        then(executor).should(times(2)).execute(any(FutureTask.class));
    }

}

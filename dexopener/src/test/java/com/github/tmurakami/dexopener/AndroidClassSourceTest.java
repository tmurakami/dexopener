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

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.dexbacked.DexBackedDexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Answer2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AndroidClassSourceTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock(stubOnly = true)
    private ClassNameFilter classNameFilter;
    @Mock(stubOnly = true)
    private DexFileLoader dexFileLoader;
    @Mock
    private Executor executor;

    @Captor
    private ArgumentCaptor<String> dexFileCaptor;

    @SuppressWarnings("deprecation")
    @Test
    public void getClassFile_should_return_the_ClassFile_with_the_given_name() throws IOException {
        final Opcodes opcodes = Opcodes.getDefault();
        given(classNameFilter.accept(matches("foo[.]Bar[\\d]{1,3}"))).willReturn(true);
        final List<Set<? extends ClassDef>> classesValues = new ArrayList<>();
        given(dexFileLoader.loadDex(dexFileCaptor.capture(), anyString()))
                .will(answer(new Answer2<dalvik.system.DexFile, String, String>() {
                    @SuppressWarnings("TryFinallyCanBeTryWithResources")
                    @Override
                    public dalvik.system.DexFile answer(String src, String out) throws IOException {
                        DexBackedDexFile file = DexBackedDexFileUtils.loadDexFile(opcodes, src);
                        final Set<? extends ClassDef> classes = file.getClasses();
                        classesValues.add(classes);
                        dalvik.system.DexFile dexFile = mock(dalvik.system.DexFile.class,
                                                             withSettings().stubOnly());
                        given(dexFile.entries()).willAnswer(new Answer<Enumeration<String>>() {
                            @Override
                            public Enumeration<String> answer(InvocationOnMock invocation) {
                                return Collections.enumeration(Collections2.transform(classes, new Function<ClassDef, String>() {
                                    @Override
                                    public String apply(@Nullable ClassDef input) {
                                        String dexName = Objects.requireNonNull(input).getType();
                                        return dexName.substring(1, dexName.length() - 1).replace('/', '.');
                                    }
                                }));
                            }
                        });
                        return dexFile;
                    }
                }));
        int classCount = 101; // DexFileHolderMapper#MAX_CLASSES_PER_DEX_FILE + 1
        List<String> classNames = new ArrayList<>(classCount);
        for (int i = 0; i < classCount; i++) {
            classNames.add("foo.Bar" + i);
        }
        Set<ImmutableClassDef> classes = new HashSet<>();
        for (String className : classNames) {
            classes.add(new ImmutableClassDef('L' + className.replace('.', '/') + ';',
                                              0,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null));
        }
        ImmutableDexFile dexFile = new ImmutableDexFile(org.jf.dexlib2.Opcodes.getDefault(), classes);
        File apk = generateZip(DexPoolUtils.toBytecode(dexFile));
        AndroidClassSource classSource = new AndroidClassSource(apk.getCanonicalPath(),
                                                                classNameFilter,
                                                                folder.newFolder(),
                                                                dexFileLoader,
                                                                executor);
        for (String className : classNames) {
            assertNotNull(classSource.getClassFile(className));
        }
        assertSame(2, dexFileCaptor.getAllValues().size());
        assertSame(2, classesValues.size());
        assertSame(100, classesValues.get(0).size());
        assertSame(1, classesValues.get(1).size());
        then(executor).should(times(2)).execute(any(FutureTask.class));
    }

    @Test
    public void getClassFile_should_return_null_if_the_given_name_does_not_pass_through_the_filter()
            throws IOException {
        AndroidClassSource classSource = new AndroidClassSource("",
                                                                classNameFilter,
                                                                folder.newFolder(),
                                                                dexFileLoader,
                                                                executor);
        assertNull(classSource.getClassFile("foo.Bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void getClassFile_should_throw_IllegalStateException_if_no_class_to_be_opened_was_found()
            throws IOException {
        String className = "foo.Bar";
        given(classNameFilter.accept(className)).willReturn(true);
        ImmutableDexFile dexFile = new ImmutableDexFile(org.jf.dexlib2.Opcodes.getDefault(),
                                                        Collections.<org.jf.dexlib2.iface.ClassDef>emptySet());
        File apk = generateZip(DexPoolUtils.toBytecode(dexFile));
        new AndroidClassSource(apk.getCanonicalPath(),
                               classNameFilter,
                               folder.newFolder(),
                               dexFileLoader,
                               executor).getClassFile(className);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private File generateZip(byte[] bytecode) throws IOException {
        File zip = folder.newFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
        try {
            out.putNextEntry(new ZipEntry("classes.dex"));
            out.write(bytecode);
        } finally {
            out.close();
        }
        return zip;
    }

}

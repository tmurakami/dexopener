package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Annotation;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.DexFile;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Field;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.Method;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableDexFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertSame;

@SuppressWarnings("deprecation")
public class DexFileGeneratorTest {

    @Rule
    public TemporaryFolder folder =
            new TemporaryFolder(InstrumentationRegistry.getTargetContext().getCacheDir());

    private final Executor executor = new Executor() {
        @Override
        public void execute(@NonNull Runnable runnable) {
            runnable.run();
        }
    };

    @Test
    public void task_should_generate_a_DexFile_of_which_final_modifiers_are_removed()
            throws Exception {
        ClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                             0,
                                             "Ljava/lang/Object;",
                                             null,
                                             null,
                                             Collections.<Annotation>emptySet(),
                                             Collections.<Field>emptySet(),
                                             Collections.<Method>emptySet());
        DexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), Collections.singleton(def));
        ClassLoader classLoader = new ClassLoader() {
        };
        DexFileGenerator generator = new DexFileGenerator(executor, folder.newFolder());
        Class<?> c = generator.generateDexFile(dexFile).get().loadClass("foo.Bar", classLoader);
        assertSame(classLoader, c.getClassLoader());
    }

    @Test(expected = IOException.class)
    public void getting_a_DexFile_should_cause_IOException_if_the_cache_directory_cannot_be_created()
            throws Throwable {
        ClassDef def = new ImmutableClassDef("Lfoo/Bar;",
                                             0,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
        DexFile dexFile = new ImmutableDexFile(Opcodes.getDefault(), Collections.singleton(def));
        DexFileGenerator generator = new DexFileGenerator(executor, folder.newFile());
        FutureTask<dalvik.system.DexFile> task = generator.generateDexFile(dexFile);
        try {
            task.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

}

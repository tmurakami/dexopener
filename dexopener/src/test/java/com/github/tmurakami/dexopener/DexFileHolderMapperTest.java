package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.Opcodes;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.iface.ClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.immutable.ImmutableClassDef;
import com.github.tmurakami.dexopener.repackaged.org.jf.dexlib2.writer.pool.DexPool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DexFileHolderMapperTest {

    @Mock
    private ClassNameFilter classNameFilter;
    @Mock
    private Executor executor;
    @Mock
    private DexFileTaskFactory dexFileTaskFactory;
    @Mock
    private FutureTask<dalvik.system.DexFile> task;
    @Mock
    private dalvik.system.DexFile dexFile;

    @Captor
    private ArgumentCaptor<Set<ClassDef>> classesToBeOpenedCaptor;

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Test
    public void map_should_collect_DexFileHolders() throws Exception {
        Opcodes opcodes = Opcodes.getDefault();
        DexPool pool = new DexPool(opcodes);
        int classCount = 151;
        for (int i = 0; i < classCount; i++) {
            String className = "foo.Bar" + i;
            pool.internClass(new ImmutableClassDef(TypeUtils.getInternalName(className),
                                                   0,
                                                   null,
                                                   null,
                                                   null,
                                                   null,
                                                   null,
                                                   null));
        }
        byte[] bytecode = DexPoolUtils.toBytecode(pool);
        given(classNameFilter.accept(anyString())).willReturn(true);
        given(dexFileTaskFactory.newDexFileTask(any(Opcodes.class),
                                                classesToBeOpenedCaptor.capture())).willReturn(task);
        given(task.get()).willReturn(dexFile);
        Map<String, DexFileHolder> holderMap = new HashMap<>();
        new DexFileHolderMapper(classNameFilter, executor, dexFileTaskFactory).map(bytecode, holderMap);
        assertEquals(classCount, holderMap.size());
        List<Set<ClassDef>> classesList = classesToBeOpenedCaptor.getAllValues();
        assertEquals(2, classesList.size());
        assertEquals(150, classesList.get(0).size());
        assertEquals(1, classesList.get(1).size());
        HashSet<DexFileHolder> holders = new HashSet<>(holderMap.values());
        assertEquals(2, holders.size());
        for (DexFileHolder holder : holders) {
            assertEquals(dexFile, holder.get());
        }
        then(task).should(times(2)).run();
        then(executor).should(times(2)).execute(task);
    }

}

package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import dalvik.system.DexFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassNameReaderTest {

    @Mock
    ClassNameFilter classNameFilter;
    @Mock
    DexFile dexFile;

    @InjectMocks
    ClassNameReader target;

    @Test
    public void testReadClassNames() {
        String name = getClass().getName() + "$C";
        given(dexFile.entries()).willReturn(Collections.enumeration(Collections.singleton(name)));
        given(classNameFilter.accept(name)).willReturn(true);
        Iterable<Set<String>> result = target.read(dexFile);
        Iterator<Set<String>> iterator = result.iterator();
        assertTrue(iterator.hasNext());
        Set<String> names = iterator.next();
        assertEquals(1, names.size());
        assertTrue(names.contains('L' + name.replace('.', '/') + ';'));
        assertFalse(iterator.hasNext());
    }

}

package com.github.tmurakami.dexopener;

import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.ApplicationWriter;
import com.github.tmurakami.dexopener.repackaged.org.ow2.asmdex.lowLevelUtils.DexFileReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassNameReaderImplTest {

    @Mock
    ClassNameFilter classNameFilter;

    @InjectMocks
    ClassNameReaderImpl target;

    @Test
    public void testReadClassNames() throws IOException {
        String name = getClass().getName() + "$C";
        given(classNameFilter.accept(name)).willReturn(true);
        DexFileReader reader = new DexFileReader();
        String internalName = 'L' + name.replace('.', '/') + ';';
        reader.parse(generateDexBytes(internalName));
        Collection<String> result = target.readClassNames(reader);
        assertSame(1, result.size());
        assertTrue(result.contains(internalName));
    }

    private static byte[] generateDexBytes(String name) {
        ApplicationWriter aw = new ApplicationWriter();
        aw.visit();
        aw.visitClass(0, name, null, "Ljava/lang/Object;", null);
        aw.visitEnd();
        return aw.toByteArray();
    }

}
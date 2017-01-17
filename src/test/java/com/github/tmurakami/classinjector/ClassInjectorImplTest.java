package com.github.tmurakami.classinjector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassInjectorImplTest {

    @Mock
    ClassSource bytecode;
    @Mock
    StealthClassLoader.Factory classLoaderFactory;
    @Mock
    StealthClassLoader stealthClassLoader;

    @InjectMocks
    ClassInjectorImpl testTarget;

    @Test
    public void into() throws Exception {
        ClassLoader injectionTarget = new ClassLoader() {
        };
        given(classLoaderFactory.create(bytecode, injectionTarget)).willReturn(stealthClassLoader);
        testTarget.into(injectionTarget);
        assertSame(stealthClassLoader, injectionTarget.getParent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void into_nullTarget() throws Exception {
        testTarget.into(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void into_alreadyInjected() throws Exception {
        testTarget.into(new ClassLoader(stealthClassLoader) {
        });
    }

}

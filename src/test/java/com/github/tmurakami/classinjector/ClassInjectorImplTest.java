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
    ClassDefiner definer;
    @Mock
    ClassSource source;
    @Mock
    StealthClassLoader.Factory classLoaderFactory;
    @Mock
    StealthClassLoader stealthClassLoader;

    @InjectMocks
    ClassInjectorImpl testTarget;

    @Test
    public void into() {
        ClassLoader injectionTarget = new ClassLoader() {
        };
        given(classLoaderFactory.create(definer, source, injectionTarget)).willReturn(stealthClassLoader);
        testTarget.into(injectionTarget);
        assertSame(stealthClassLoader, injectionTarget.getParent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void into_nullTarget() {
        testTarget.into(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void into_alreadyInjected() {
        testTarget.into(new ClassLoader(stealthClassLoader) {
        });
    }

}

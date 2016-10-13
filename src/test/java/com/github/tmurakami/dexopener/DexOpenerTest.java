package com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DexOpenerTest {

    @Mock
    DexInstaller dexInstaller;
    @Mock
    DexOpener.SuperCalls superCalls;
    @Mock
    Context targetContext;

    @InjectMocks
    DexOpener target;

    @Test
    public void testOnCreate() throws IOException {
        given(superCalls.getTargetContext()).willReturn(targetContext);
        Bundle arguments = new Bundle();
        target.onCreate(arguments);
        InOrder inOrder = inOrder(dexInstaller, superCalls);
        then(dexInstaller).should(inOrder).install(targetContext);
        then(superCalls).should(inOrder).onCreate(arguments);
    }

    @Test
    public void testNewApplication() throws Exception {
        given(superCalls.getTargetContext()).willReturn(targetContext);
        ClassLoader cl = mock(ClassLoader.class);
        Context context = mock(Context.class);
        Application application = new Application();
        given(superCalls.newApplication(cl, "a", context)).willReturn(application);
        assertSame(application, target.newApplication(cl, "a", context));
        then(dexInstaller).should().install(targetContext);
    }

}

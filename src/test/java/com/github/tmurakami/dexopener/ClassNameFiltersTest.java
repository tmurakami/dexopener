package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClassNameFiltersTest {

    @Mock
    ClassNameFilter filter;

    @Test
    public void accept() throws Exception {
        assertFalse(new ClassNameFilters(Collections.singleton(filter)).accept("foo.Bar"));
    }

    @Test
    public void accept_matched() throws Exception {
        given(filter.accept("foo.Bar")).willReturn(true);
        assertTrue(new ClassNameFilters(Collections.singleton(filter)).accept("foo.Bar"));
    }

}

package com.github.tmurakami.dexopener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ClassNameFiltersTest {

    @Mock
    ClassNameFilter filter;

    @Test
    public void the_accept_method_should_return_false_if_the_class_name_does_not_pass_any_ClassNameFilters() throws Exception {
        assertFalse(new ClassNameFilters(Collections.singleton(filter)).accept("foo.Bar"));
    }

    @Test
    public void the_accept_method_should_return_true_if_the_class_name_passes_one_of_the_ClassNameFilters() throws Exception {
        given(filter.accept("foo.Bar")).willReturn(true);
        assertTrue(new ClassNameFilters(Collections.singleton(filter)).accept("foo.Bar"));
    }

}

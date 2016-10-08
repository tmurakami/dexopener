package test.com.github.tmurakami.dexopener;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContextTest {

    @Rule
    public final ActivityTestRule<Activity> rule = new ActivityTestRule<>(Activity.class);

    @Test
    public void testGetClassLoader() {
        ClassLoader classLoader = getClass().getClassLoader();
        assertEquals(classLoader, rule.getActivity().getClassLoader());
        Context context = InstrumentationRegistry.getContext();
        assertEquals(classLoader, context.getClassLoader());
        assertEquals(classLoader, context.getApplicationContext().getClassLoader());
        assertEquals(classLoader, InstrumentationRegistry.getTargetContext().getClassLoader());
    }

}

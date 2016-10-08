package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.ContextWrapper;

import java.lang.reflect.Field;

final class ContextWrapperHelper {

    private static final Field M_BASE;

    static {
        try {
            M_BASE = ContextWrapper.class.getDeclaredField("mBase");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    private ContextWrapperHelper() {
        throw new AssertionError("Do not instantiate");
    }

    static void setBaseContext(ContextWrapper context, Context base) {
        M_BASE.setAccessible(true);
        try {
            M_BASE.set(context, base);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

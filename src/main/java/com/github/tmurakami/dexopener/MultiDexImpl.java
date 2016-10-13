package com.github.tmurakami.dexopener;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class MultiDexImpl implements MultiDex {

    private static final Method INSTALL;

    static {
        Method install = null;
        try {
            install = Class.forName("android.support.multidex.MultiDex").getMethod("install", Context.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        } catch (ClassNotFoundException ignored) {
        }
        INSTALL = install;
    }

    @Override
    public void install(Context context) {
        if (INSTALL == null) {
            return;
        }
        while (true) {
            INSTALL.setAccessible(true);
            try {
                INSTALL.invoke(null, context);
                return;
            } catch (IllegalAccessException ignored) {
            } catch (InvocationTargetException e) {
                throw MultiDexImpl.<RuntimeException>sneakyThrow(e.getCause());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }

}

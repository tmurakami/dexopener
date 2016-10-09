package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.ContextWrapper;

final class InternalContextWrapper extends ContextWrapper {

    private final Context base;
    private final ClassLoader classLoader;

    InternalContextWrapper(Context base, ClassLoader classLoader) {
        super(base);
        this.base = base;
        this.classLoader = classLoader;
    }

    @Override
    public Context getBaseContext() {
        return base;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

}

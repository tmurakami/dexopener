package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.ContextWrapper;

final class InternalContextWrapper extends ContextWrapper {

    private final ClassLoader classLoader;

    InternalContextWrapper(Context base, ClassLoader classLoader) {
        super(base);
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

}

package com.github.tmurakami.dexopener;

import android.app.Instrumentation;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DexOpenerBuilderImpl implements DexOpener.Builder {

    private final List<ClassNameFilter> classNameFilters = new ArrayList<>();

    @NonNull
    @Override
    public DexOpener.Builder classNameFilters(@NonNull ClassNameFilter... filters) {
        for (ClassNameFilter f : filters) {
            if (f == null) {
                throw new IllegalArgumentException("'filters' contains null");
            }
            classNameFilters.add(f);
        }
        return this;
    }

    @NonNull
    @Override
    public DexOpener build(@NonNull Instrumentation instrumentation) {
        List<ClassNameFilter> filters = Collections.unmodifiableList(classNameFilters);
        return new DexOpenerImpl(instrumentation, new ClassNameFilters(filters));
    }

}

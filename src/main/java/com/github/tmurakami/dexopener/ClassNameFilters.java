package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

final class ClassNameFilters implements ClassNameFilter {

    private final Iterable<? extends ClassNameFilter> classNameFilters;

    ClassNameFilters(Iterable<? extends ClassNameFilter> classNameFilters) {
        this.classNameFilters = classNameFilters;
    }

    @Override
    public boolean accept(@NonNull String className) {
        for (ClassNameFilter f : classNameFilters) {
            if (f.accept(className)) {
                return true;
            }
        }
        return false;
    }

}

package com.github.tmurakami.dexopener;

import android.support.annotation.NonNull;

final class AcceptAll implements ClassNameFilter {

    static final ClassNameFilter INSTANCE = new AcceptAll();

    private AcceptAll() {
    }

    @Override
    public boolean accept(@NonNull String className) {
        return true;
    }

}

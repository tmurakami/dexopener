package com.github.tmurakami.dexopener;

import android.content.Context;
import android.support.multidex.MultiDex;

final class MultiDexHelperImpl implements MultiDexHelper {
    @Override
    public void installMultiDex(Context context) {
        try {
            Class.forName("android.support.multidex.MultiDex");
            MultiDex.install(context);
        } catch (ClassNotFoundException ignored) {
        }
    }
}

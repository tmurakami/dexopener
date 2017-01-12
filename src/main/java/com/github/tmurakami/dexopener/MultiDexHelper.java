package com.github.tmurakami.dexopener;

import android.content.Context;
import android.support.multidex.MultiDex;

final class MultiDexHelper {
    void install(Context context) {
        try {
            MultiDex.install(context);
        } catch (NoClassDefFoundError ignored) {
        }
    }
}

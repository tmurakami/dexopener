package com.example.dexopener;

import android.content.Context;

final class MyService {
    final String getString(Context context) {
        return context.getString(R.string.app_name);
    }
}

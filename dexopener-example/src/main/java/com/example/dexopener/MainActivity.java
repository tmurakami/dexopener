package com.example.dexopener;

import android.app.Activity;
import android.os.Bundle;

public final class MainActivity extends Activity {

    MainActivityDelegate delegate = new MainActivityDelegate(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate.onCreate(savedInstanceState);
    }

}

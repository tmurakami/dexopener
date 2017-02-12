package com.example.dexopener;

import android.app.Activity;
import android.os.Bundle;

public final class MainActivity extends Activity {

    MainService service = new MainService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(service.getString(this));
    }

}

package com.example.dexopener;

import android.os.Bundle;

final class MainActivityDelegate {

    private final MainActivity activity;

    MainActivityDelegate(MainActivity activity) {
        this.activity = activity;
    }

    void onCreate(@SuppressWarnings("unused") Bundle savedInstanceState) {
        activity.setTitle("MainActivity");
    }

}

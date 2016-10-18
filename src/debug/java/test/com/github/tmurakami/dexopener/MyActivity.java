package test.com.github.tmurakami.dexopener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;

public final class MyActivity extends FragmentActivity {

    private final MyFragment fragment;

    @SuppressWarnings("unused")
    public MyActivity() {
        this(new MyFragment());
    }

    @VisibleForTesting
    MyActivity(MyFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().add(fragment, null).commitNow();
    }

}

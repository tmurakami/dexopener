package test.com.github.tmurakami.dexopener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;

public final class MyActivity extends FragmentActivity {

    private final MyService service;

    @VisibleForTesting
    Object result;

    @SuppressWarnings("unused")
    public MyActivity() {
        this(new MyService());
    }

    @VisibleForTesting
    MyActivity(MyService service) {
        this.service = service;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        result = service.doIt();
    }

}

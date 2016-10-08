package test.com.github.tmurakami.dexopener;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

public class MyActivity extends Activity {

    @VisibleForTesting
    MyService service = new MyService();

    @VisibleForTesting
    Object result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        result = service.doIt();
    }

}

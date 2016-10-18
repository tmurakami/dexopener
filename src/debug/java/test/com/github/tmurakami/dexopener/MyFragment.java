package test.com.github.tmurakami.dexopener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;

public final class MyFragment extends Fragment {

    private MyService service = new MyService();

    @VisibleForTesting
    Object result;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        result = service.doIt();
    }

}

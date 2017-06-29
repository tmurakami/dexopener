package test.com.github.tmurakami.dexopener;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnitRunner;

import com.github.tmurakami.dexopener.ClassNameFilter;
import com.github.tmurakami.dexopener.DexOpener;

public class JUnitRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DexOpener.builder(context).classNameFilter(new ClassNameFilter() {
            @Override
            public boolean accept(@NonNull String className) {
                return className.startsWith("test.com.github.tmurakami.dexopener.");
            }
        }).build().installTo(cl);
        return super.newApplication(cl, className, context);
    }
}

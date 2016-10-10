package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.File;
import java.io.IOException;

interface DexOpenerDelegateHelper {

    void setBaseContext(ContextWrapper context, Context base);

    void setContextClassLoader(Thread thread, ClassLoader classLoader);

    ClassLoader newClassLoader(String apkPath,
                               String testApkPath,
                               File cacheDir,
                               ClassLoader parent) throws IOException;

}

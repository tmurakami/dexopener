/*
 * Copyright 2016 Tsuyoshi Murakami
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tmurakami.dexopener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.github.tmurakami.dexopener.repackaged.com.github.tmurakami.classinjector.ClassInjector;

import java.io.File;

import androidx.annotation.NonNull;

final class DexOpenerImpl extends DexOpener {

    private final Context context;
    private final AndroidClassSourceFactory androidClassSourceFactory;

    DexOpenerImpl(Context context, AndroidClassSourceFactory androidClassSourceFactory) {
        this.context = context;
        this.androidClassSourceFactory = androidClassSourceFactory;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void installTo(@NonNull ClassLoader target) {
        Context context = this.context;
        if (context.getApplicationContext() != null) {
            throw new IllegalStateException(
                    "This method must be called before the Application instance is created");
        }
        ApplicationInfo ai = context.getApplicationInfo();
        File parentDir;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            parentDir = new File(ai.dataDir, "code_cache");
        } else {
            parentDir = context.getCodeCacheDir();
        }
        File cacheDir = new File(parentDir, "dexopener");
        if (cacheDir.isDirectory() || cacheDir.mkdirs()) {
            FileUtils.delete(cacheDir.listFiles());
        }
        ClassInjector.from(androidClassSourceFactory.newClassSource(ai.sourceDir, cacheDir))
                     .into(target);
    }

}

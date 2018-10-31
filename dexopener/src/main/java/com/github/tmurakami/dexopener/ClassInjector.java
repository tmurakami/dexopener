/*
 * Copyright 2017 Tsuyoshi Murakami
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

import android.os.Build;

import java.lang.reflect.Field;

@SuppressWarnings("JavaReflectionMemberAccess")
final class ClassInjector {

    private static Field parentField;
    private static NoSuchFieldException noSuchFieldException;

    static {
        try {
            parentField = ClassLoader.class.getDeclaredField("parent");
        } catch (NoSuchFieldException e) {
            noSuchFieldException = e;
        }
    }

    private final InjectorClassLoaderFactory injectorClassLoaderFactory;

    ClassInjector(InjectorClassLoaderFactory injectorClassLoaderFactory) {
        this.injectorClassLoaderFactory = injectorClassLoaderFactory;
    }

    void into(ClassLoader target) {
        ClassLoader parent = target.getParent();
        if (target instanceof InjectorClassLoader || parent instanceof InjectorClassLoader) {
            throw new IllegalArgumentException("'target' has already been injected");
        }
        Exception exception = noSuchFieldException;
        if (exception == null) {
            Field f = parentField;
            ClassLoader loader = injectorClassLoaderFactory.newInjectorClassLoader(parent, target);
            try {
                while (true) {
                    f.setAccessible(true);
                    try {
                        f.set(target, loader);
                        return;
                    } catch (IllegalAccessException ignored) {
                    }
                }
            } catch (Exception e) {
                exception = e;
            }
        }
        throw new UnsupportedOperationException(
                "Unsupported environment\n" +
                "    Device : " + Build.DEVICE + '\n' +
                "    API    : " + Build.VERSION.SDK_INT + '\n' +
                "    Brand  : " + Build.BRAND + '\n' +
                "    Model  : " + Build.MODEL,
                exception);
    }

}

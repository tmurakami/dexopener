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

import java.io.IOError;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.tmurakami.dexopener.Constants.MY_PACKAGE_PREFIX;

final class InjectorClassLoader extends ClassLoader {

    private final ClassSource source;
    private final ClassLoader injectionTarget;
    private final Set<String> findingClasses = new HashSet<>();

    InjectorClassLoader(ClassLoader parent, ClassSource source, ClassLoader injectionTarget) {
        super(parent);
        this.source = source;
        this.injectionTarget = injectionTarget;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = injectClass(name);
        return c == null ? super.findClass(name) : c;
    }

    private Class<?> injectClass(String name) {
        if (name.startsWith(MY_PACKAGE_PREFIX)) {
            return null;
        }
        Set<String> classes = findingClasses;
        if (classes.contains(name)) {
            throw new IllegalStateException(name + " is recursively loaded");
        }
        ClassFile f;
        try {
            f = source.getClassFile(name);
        } catch (IOException e) {
            throw new IOError(e);
        }
        if (f == null) {
            return null;
        }
        classes.add(name);
        try {
            Class<?> c = f.toClass(injectionTarget);
            Logger logger = Loggers.get();
            if (logger.isLoggable(Level.FINEST)) {
                String hash = Integer.toHexString(System.identityHashCode(injectionTarget));
                String target = injectionTarget.getClass().getName() + '@' + hash;
                logger.finest("The class " + c.getName() + " was injected into " + target);
            }
            return c;
        } finally {
            classes.remove(name);
        }
    }

}

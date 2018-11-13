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

import java.util.logging.Level;
import java.util.logging.Logger;

class ClassInjector extends ClassLoader {

    private final ClassLoader target;
    private final ClassPath classPath;

    ClassInjector(ClassLoader target, ClassPath classPath) {
        super(target.getParent());
        this.target = target;
        this.classPath = classPath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = classPath.loadClass(name, target);
        if (c == null) {
            return super.findClass(name);
        }
        Logger logger = Loggers.get();
        if (logger.isLoggable(Level.FINEST)) {
            String hash = Integer.toHexString(System.identityHashCode(target));
            logger.finest("Injected " + name + " into " + target.getClass().getName() + '@' + hash);
        }
        return c;
    }

}

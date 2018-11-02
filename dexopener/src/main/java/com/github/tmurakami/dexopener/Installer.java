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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Installer {

    private final ClassPath classPath;

    Installer(ClassPath classPath) {
        this.classPath = classPath;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    void installTo(ClassLoader target) {
        for (ClassLoader l = target; l != null; l = l.getParent()) {
            if (l instanceof Injector) {
                throw new IllegalStateException("Already installed");
            }
        }
        Field parentField;
        try {
            parentField = ClassLoader.class.getDeclaredField("parent");
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException(e);
        }
        Injector injector = new Injector(target, classPath);
        do {
            parentField.setAccessible(true);
            try {
                parentField.set(target, injector);
            } catch (IllegalAccessException ignored) {
            }
        } while (target.getParent() != injector);
    }

    private static final class Injector extends ClassLoader {

        private final ClassLoader target;
        private final ClassPath classPath;

        private Injector(ClassLoader target, ClassPath classPath) {
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
                String target = this.target.getClass().getName() + '@' + hash;
                logger.finest("The class " + c.getName() + " was injected into " + target);
            }
            return c;
        }

    }

}

package com.github.tmurakami.classinjector;

import java.io.IOError;
import java.io.IOException;

final class StealthClassLoader extends ClassLoader {

    private static final String MY_PACKAGE = "com.github.tmurakami.classinjector.";

    private final ClassDefiner definer;
    private final ClassSource source;
    private final ClassLoader injectionTarget;

    StealthClassLoader(ClassDefiner definer,
                       ClassSource source,
                       ClassLoader injectionTarget) {
        super(injectionTarget.getParent());
        this.definer = definer;
        this.source = source;
        this.injectionTarget = injectionTarget;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!name.startsWith(MY_PACKAGE)) {
            byte[] bytecode;
            try {
                bytecode = source.getBytecodeFor(name);
            } catch (IOException e) {
                throw new IOError(e);
            }
            if (bytecode != null) {
                Class<?> c = definer.defineClass(name, bytecode, injectionTarget);
                if (c == null) {
                    throw new NoClassDefFoundError(name);
                }
                return c;
            }
        }
        return super.findClass(name);
    }

    static final class Factory {
        ClassLoader create(ClassDefiner definer, ClassSource source, ClassLoader injectionTarget) {
            return new StealthClassLoader(definer, source, injectionTarget);
        }
    }

}

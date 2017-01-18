package com.github.tmurakami.classinjector;

import java.lang.reflect.Field;

final class ClassInjectorImpl extends ClassInjector {

    private static final Field PARENT_FIELD;

    static {
        String name = "parent";
        try {
            PARENT_FIELD = ClassLoader.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("This version of java is not supported", e);
        }
    }

    private final ClassSource source;
    private final StealthClassLoader.Factory classLoaderFactory;

    ClassInjectorImpl(ClassSource source, StealthClassLoader.Factory classLoaderFactory) {
        this.source = source;
        this.classLoaderFactory = classLoaderFactory;
    }

    @Override
    public void into(ClassLoader target) {
        if (target == null) {
            throw new IllegalArgumentException("'target' is null");
        }
        for (ClassLoader l = target; l != null; l = l.getParent()) {
            if (l instanceof StealthClassLoader) {
                throw new IllegalArgumentException(ClassSource.class.getSimpleName()
                        + " has already been injected into " + target);
            }
        }
        ClassLoader classLoader = classLoaderFactory.create(source, target);
        Field f = PARENT_FIELD;
        try {
            f.setAccessible(true);
            f.set(target, classLoader);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot inject into " + target, e);
        } catch (RuntimeException e) {
            if (!e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
                throw e;
            }
            Unsafe unsafe = Unsafe.getUnsafe();
            unsafe.putObject(target, unsafe.objectFieldOffset(f), classLoader);
        }
    }

}

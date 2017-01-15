package com.github.tmurakami.classinjector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class ClassInjectorImpl extends ClassInjector {

    private static final Field PARENT;

    static {
        String name = "parent";
        try {
            PARENT = ClassLoader.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError("Field " + name
                    + " is not found in class " + ClassLoader.class.getName());
        }
    }

    private final ClassDefiner definer;
    private final ClassSource source;
    private final StealthClassLoader.Factory classLoaderFactory;

    ClassInjectorImpl(ClassDefiner definer,
                      ClassSource source,
                      StealthClassLoader.Factory classLoaderFactory) {
        this.definer = definer;
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
        PARENT.setAccessible(true);
        try {
            PARENT.set(target, classLoaderFactory.create(definer, source, target));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError("Cannot access field "
                    + PARENT.getDeclaringClass().getName() + '#' + PARENT.getName()
                    + " with modifiers '" + Modifier.toString(PARENT.getModifiers()) + "'");
        }
    }

}

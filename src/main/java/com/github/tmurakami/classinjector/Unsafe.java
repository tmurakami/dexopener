package com.github.tmurakami.classinjector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class Unsafe {

    private static final Unsafe UNSAFE = newUnsafe();

    private final Object unsafe;
    private final Method objectFieldOffset;
    private final Method putObject;

    private Unsafe(Object unsafe, Method objectFieldOffset, Method putObject) {
        this.unsafe = unsafe;
        this.objectFieldOffset = objectFieldOffset;
        this.putObject = putObject;
    }

    long objectFieldOffset(Field f) {
        try {
            return (Long) objectFieldOffset.invoke(unsafe, f);
        } catch (IllegalAccessException e) {
            throw asUncheckedError(e);
        } catch (InvocationTargetException e) {
            throw asUncheckedError(e.getCause());
        }
    }

    void putObject(Object o, long offset, Object x) {
        try {
            putObject.invoke(unsafe, o, offset, x);
        } catch (IllegalAccessException e) {
            throw asUncheckedError(e);
        } catch (InvocationTargetException e) {
            throw asUncheckedError(e.getCause());
        }
    }

    static Unsafe getUnsafe() {
        return UNSAFE;
    }

    private static Error asUncheckedError(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        }
        return new Error("Unexpected error: " + t.getMessage(), t);
    }

    private static Object getTheUnsafe(Class<?> c) {
        String name = "theUnsafe";
        Field f;
        try {
            f = c.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError("Field " + name + " is not found in class " + c.getName());
        }
        f.setAccessible(true);
        try {
            return f.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError("Cannot access field "
                    + f.getDeclaringClass().getName() + '#' + f.getName()
                    + " with modifiers '" + Modifier.toString(f.getModifiers()) + "'");
        }
    }

    private static Method getObjectFieldOffsetMethod(Class<?> c) {
        String name = "objectFieldOffset";
        try {
            return c.getMethod("objectFieldOffset", Field.class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError("Field " + name + " is not found in class " + c.getName());
        }
    }

    private static Method getPutObjectMethod(Class<?> c) {
        String name = "putObject";
        try {
            return c.getMethod(name, Object.class, long.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError("Field " + name + " is not found in class " + c.getName());
        }
    }

    private static Unsafe newUnsafe() {
        String name = "sun.misc.Unsafe";
        Class<?> c;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(name);
        }
        Object unsafe = getTheUnsafe(c);
        Method objectFieldOffsetMethod = getObjectFieldOffsetMethod(c);
        Method putObjectMethod = getPutObjectMethod(c);
        return new Unsafe(unsafe, objectFieldOffsetMethod, putObjectMethod);
    }

}

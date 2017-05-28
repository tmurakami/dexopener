package com.github.tmurakami.dexopener;

final class TypeUtils {

    private TypeUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static String getClassName(String internalName) {
        return internalName.substring(1, internalName.length() - 1).replace('/', '.');
    }

    static String getInternalName(String className) {
        return 'L' + className.replace('.', '/') + ';';
    }

}

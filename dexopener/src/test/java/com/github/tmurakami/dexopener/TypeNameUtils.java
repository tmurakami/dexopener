package com.github.tmurakami.dexopener;

final class TypeNameUtils {

    private TypeNameUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static String javaToDexName(String javaName) {
        // The `javaName` must be neither a primitive type nor an array type.
        return 'L' + javaName.replace('.', '/') + ';';
    }

}

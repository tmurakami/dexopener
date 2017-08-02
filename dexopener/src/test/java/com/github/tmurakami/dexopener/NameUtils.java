package com.github.tmurakami.dexopener;

final class NameUtils {

    private NameUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static String javaToDexName(String javaName) {
        return 'L' + javaName.replace('.', '/') + ';';
    }

}

package com.github.tmurakami.dexopener;

final class DexUtils {

    private DexUtils() {
        throw new AssertionError("Do not instantiate");
    }

    static String toClassName(String internalName) {
        return internalName.substring(1, internalName.length() - 1).replace('/', '.');
    }

    static String toInternalName(String className) {
        return 'L' + className.replace('.', '/') + ';';
    }

}

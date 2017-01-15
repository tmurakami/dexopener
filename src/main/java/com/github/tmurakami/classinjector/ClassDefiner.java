package com.github.tmurakami.classinjector;

public interface ClassDefiner {
    Class defineClass(String name, byte[] bytecode, ClassLoader classLoader);
}

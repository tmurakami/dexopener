package com.github.tmurakami.dexopener;

interface ClassLoaderFactory {
    ClassLoader newClassLoader(Iterable<String> dexPaths, String optimizedDirectory, ClassLoader parent);
}

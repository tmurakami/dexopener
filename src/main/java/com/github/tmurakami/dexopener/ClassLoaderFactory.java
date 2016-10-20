package com.github.tmurakami.dexopener;

interface ClassLoaderFactory {
    ClassLoader newClassLoader(ClassLoader classLoader, DexElement element);
}

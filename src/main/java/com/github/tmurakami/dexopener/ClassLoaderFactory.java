package com.github.tmurakami.dexopener;

interface ClassLoaderFactory {
    ClassLoader newClassLoader(ClassLoader classLoader, Iterable<Dex> dexes);
}

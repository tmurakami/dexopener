package com.github.tmurakami.dexopener;

import java.util.List;

interface ClassLoaderFactory {
    ClassLoader newClassLoader(ClassLoader classLoader, List<Dex> dices);
}

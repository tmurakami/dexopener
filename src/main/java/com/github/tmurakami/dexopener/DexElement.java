package com.github.tmurakami.dexopener;

interface DexElement {
    Class loadClass(String name, ClassLoader classLoader);
}

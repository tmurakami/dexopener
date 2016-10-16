package com.github.tmurakami.dexopener;

interface Dex {
    Class loadClass(String name, ClassLoader classLoader);
}

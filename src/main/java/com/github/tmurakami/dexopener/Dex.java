package com.github.tmurakami.dexopener;

import java.io.IOException;

interface Dex {
    Class loadClass(String name, ClassLoader classLoader) throws IOException;
}

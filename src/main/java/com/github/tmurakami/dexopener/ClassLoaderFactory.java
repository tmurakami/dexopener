package com.github.tmurakami.dexopener;

import java.io.File;
import java.io.IOException;

interface ClassLoaderFactory {
    ClassLoader newClassLoader(ClassLoader parent,
                               File optimizedDirectory,
                               String... dexPaths) throws IOException;
}

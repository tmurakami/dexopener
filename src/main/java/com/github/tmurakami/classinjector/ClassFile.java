package com.github.tmurakami.classinjector;

import java.io.Closeable;
import java.io.IOException;

/**
 * An object representing data making up a class.
 */
public interface ClassFile extends Closeable {

    /**
     * Create an instance of {@link Class} with the given class loader.
     *
     * @param classLoader The {@link ClassLoader} to use for creating a class
     * @return The {@link Class} object.
     * @throws IOException If an IO error occurred.
     */
    Class toClass(ClassLoader classLoader) throws IOException;

}

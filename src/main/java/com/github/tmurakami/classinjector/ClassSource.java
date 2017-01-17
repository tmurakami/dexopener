package com.github.tmurakami.classinjector;

import java.io.IOException;

/**
 * An object representing a group of {@link ClassFile} objects.
 */
public interface ClassSource {

    /**
     * Open a {@link ClassFile} with the given name.
     *
     * @param className The class name
     * @return The {@link ClassFile}, or null if not found.
     * @throws IOException If an IO error occurred.
     */
    ClassFile getClassFile(String className) throws IOException;

}
